package accordion_symphonic.ticketing.ticket.service;

import accordion_symphonic.ticketing.availability.TicketAvailabilityService;
import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.concert.exception.ConcertNotFoundException;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.PublicOrderIdGenerator;
import accordion_symphonic.ticketing.order.dto.OrderItemRequest;
import accordion_symphonic.ticketing.order.exception.DuplicateTicketCategoryException;
import accordion_symphonic.ticketing.order.service.OrderAccessTokenService;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import accordion_symphonic.ticketing.ticket.dto.TicketBatchRequest;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import accordion_symphonic.ticketing.ticketcategory.exception.TicketCategoryNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrintedTicketBatchService {

    private final ConcertRepository concertRepository;
    private final OrderRepository orderRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketRepository ticketRepository;

    private final TicketAvailabilityService ticketAvailabilityService;
    private final TicketPdfService ticketPdfService;
    private final PrintSheetPdfService printSheetPdfService;
    private final OrderAccessTokenService orderAccessTokenService;
    private final PublicOrderIdGenerator publicOrderIdGenerator;

    public PrintedTicketBatchService(
            ConcertRepository concertRepository,
            OrderRepository orderRepository,
            TicketCategoryRepository ticketCategoryRepository,
            TicketRepository ticketRepository,
            TicketAvailabilityService ticketAvailabilityService,
            TicketPdfService ticketPdfService,
            PrintSheetPdfService printSheetPdfService,
            OrderAccessTokenService orderAccessTokenService,
            PublicOrderIdGenerator publicOrderIdGenerator
    ) {
        this.concertRepository = concertRepository;
        this.orderRepository = orderRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.ticketRepository = ticketRepository;
        this.ticketAvailabilityService = ticketAvailabilityService;
        this.ticketPdfService = ticketPdfService;
        this.printSheetPdfService = printSheetPdfService;
        this.orderAccessTokenService = orderAccessTokenService;
        this.publicOrderIdGenerator = publicOrderIdGenerator;
    }

    @Transactional
    public byte[] createPrintableBatch(
            Long concertId,
            TicketBatchRequest request
    ) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() ->
                        new ConcertNotFoundException(concertId)
                );

        OrderAccessTokenService.GeneratedOrderAccessToken accessToken =
                orderAccessTokenService.generateToken();

        Order printOrder = Order.createPrintOrder(
                concert,
                accessToken.tokenHash(),
                publicOrderIdGenerator.generate()
        );

        List<Ticket> tickets = new ArrayList<>();

        Set<Long> requestedCategoryIds = new HashSet<>();

        List<OrderItemRequest> orderedItems =
                request.items().stream()
                        .sorted(
                                Comparator.comparing(
                                        OrderItemRequest::ticketCategoryId
                                )
                        )
                        .toList();

        for (OrderItemRequest itemRequest : orderedItems) {

            if (!requestedCategoryIds.add(
                    itemRequest.ticketCategoryId()
            )) {
                throw new DuplicateTicketCategoryException(
                        itemRequest.ticketCategoryId()
                );
            }

            TicketCategory category =
                    ticketCategoryRepository
                            .findByIdAndConcertIdForUpdate(
                                    itemRequest.ticketCategoryId(),
                                    concertId
                            )
                            .orElseThrow(() ->
                                    new TicketCategoryNotFoundException(
                                            itemRequest.ticketCategoryId()
                                    )
                            );

            ticketAvailabilityService.ensureTicketsAvailable(
                    category.getId(),
                    category.getCapacity(),
                    itemRequest.quantity()
            );

            OrderItem orderItem = new OrderItem(
                    category,
                    itemRequest.quantity(),
                    category.getPrice()
            );

            printOrder.addItem(orderItem);

            for (int i = 0; i < itemRequest.quantity(); i++) {
                tickets.add(
                        new Ticket(
                                printOrder,
                                category
                        )
                );
            }
        }

        orderRepository.save(printOrder);

        List<Ticket> savedTickets =
                ticketRepository.saveAll(tickets);

        List<byte[]> individualPdfs =
                savedTickets.stream()
                        .map(ticket ->
                                ticketPdfService.createTicketPdf(
                                        printOrder,
                                        List.of(
                                                TicketResponse.fromEntity(ticket)
                                        )
                                )
                        )
                        .toList();

        return printSheetPdfService.createPrintSheet(
                individualPdfs
        );
    }
}