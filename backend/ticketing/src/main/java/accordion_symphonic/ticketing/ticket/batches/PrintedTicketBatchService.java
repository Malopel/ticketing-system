package accordion_symphonic.ticketing.ticket.batches;

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
import accordion_symphonic.ticketing.ticket.batches.dto.TicketBatchRequest;
import accordion_symphonic.ticketing.ticket.batches.dto.TicketBatchResponse;
import accordion_symphonic.ticketing.ticket.dto.TicketResponse;
import accordion_symphonic.ticketing.ticket.service.PrintSheetPdfService;
import accordion_symphonic.ticketing.ticket.service.TicketPdfService;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;
import accordion_symphonic.ticketing.ticketcategory.exception.TicketCategoryNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PrintedTicketBatchService {

    private final ConcertRepository concertRepository;
    private final OrderRepository orderRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketRepository ticketRepository;
    private final PrintedTicketBatchRepository printedTicketBatchRepository;

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
            PrintedTicketBatchRepository printedTicketBatchRepository,
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
        this.printedTicketBatchRepository = printedTicketBatchRepository;
        this.ticketAvailabilityService = ticketAvailabilityService;
        this.ticketPdfService = ticketPdfService;
        this.printSheetPdfService = printSheetPdfService;
        this.orderAccessTokenService = orderAccessTokenService;
        this.publicOrderIdGenerator = publicOrderIdGenerator;
    }

    @Transactional
    public byte[] getPrintableBatch(
            Long concertId,
            Long batchId,
            TicketBatchPrintLayout layout
    ) {
        PrintedTicketBatch batch =
                printedTicketBatchRepository
                        .findByIdAndConcertId(
                                batchId,
                                concertId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Printed ticket batch not found: " +
                                                batchId
                                )
                        );

        Order printOrder =
                batch.getOrder();

        List<Ticket> tickets = this.ticketRepository.findByOrderId(printOrder.getId());

        List<byte[]> individualPdfs =
                tickets.stream()
                        .map(ticket ->
                                ticketPdfService.createTicketPdf(
                                        printOrder,
                                        List.of(
                                                TicketResponse.fromEntity(ticket)
                                        ),
                                        layout
                                )
                        )
                        .toList();

        return printSheetPdfService.createPrintSheet(
                individualPdfs,
                layout
        );
    }

    @Transactional
    public TicketBatchResponse createPrintableBatch(Long concertId, @Valid TicketBatchRequest ticketBatchRequest) {
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
                ticketBatchRequest.items().stream()
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

        Order savedOrder =
                orderRepository.save(printOrder);

        List<Ticket> savedTickets =
                ticketRepository.saveAll(tickets);

        PrintedTicketBatch batch =
                new PrintedTicketBatch(
                        concert,
                        savedOrder,
                        ticketBatchRequest.label()
                );

        PrintedTicketBatch savedBatch =
                printedTicketBatchRepository.save(batch);

        return new TicketBatchResponse(
                savedBatch.getId(),
                savedBatch.getLabel(),
                savedTickets.size()
        );
    }

    @Transactional
    public List<TicketBatchResponse> getAllBatches(Long concertId) {
        concertRepository.findById(concertId)
                .orElseThrow(() ->
                        new ConcertNotFoundException(concertId)
                );

        return printedTicketBatchRepository
                .findByConcertId(concertId)
                .stream()
                .map(batch -> {
                    int ticketCount =
                            ticketRepository
                                    .findByOrderId(
                                            batch.getOrder().getId()
                                    )
                                    .size();

                    return new TicketBatchResponse(
                            batch.getId(),
                            batch.getLabel(),
                            ticketCount
                    );
                })
                .toList();
    }
}