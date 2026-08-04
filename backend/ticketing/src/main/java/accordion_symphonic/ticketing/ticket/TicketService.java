package accordion_symphonic.ticketing.ticket;

import accordion_symphonic.ticketing.concert.ConcertNotFoundException;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.order.OrderNotFoundException;
import accordion_symphonic.ticketing.order.OrderRepository;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    private final ConcertRepository conRepo;
    private final OrderRepository orderRepo;

    public TicketService(TicketRepository ticketRepository, ConcertRepository conRepo, OrderRepository orderRepo) {
        this.ticketRepository = ticketRepository;
        this.conRepo = conRepo;
        this.orderRepo = orderRepo;
    }

    public List<TicketResponse> createTicketsForOrder(Order order) {
        if (ticketRepository.existsByOrderId(order.getId())) {
            return ticketRepository.findByOrderId(order.getId())
                    .stream()
                    .map(TicketResponse::fromEntity)
                    .toList();
        }

        List<Ticket> tickets = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                tickets.add(new Ticket(order, item.getTicketCategory()));
            }
        }

        return ticketRepository.saveAll(tickets)
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public List<TicketResponse> getTicketsByConcertIdAndOrderId(Long concertId, Long orderId) {
        if (!conRepo.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        Order order = orderRepo.findByIdAndConcertId(orderId, concertId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return ticketRepository.findByOrderId(order.getId())
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public TicketResponse validateTicket(Long concertId, String qrToken) {
        Ticket ticket = findTicketForConcert(concertId, qrToken);

        return TicketResponse.fromEntity(ticket);
    }

    public TicketResponse useTicket(Long concertId, String qrToken) {
        Ticket ticket  = findTicketForConcert(concertId, qrToken);

        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new TicketIsNotValidException(qrToken);
        }

        ticket.useTicket();

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.fromEntity(savedTicket);
    }

    public byte[] generateQrCodePng(Long concertId, String qrToken) {
        findTicketForConcert(concertId, qrToken);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrToken,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("QR-Code konnte nicht erzeugt werden.", exception);
        }
    }

    private Ticket findTicketForConcert(Long concertId, String qrToken) {
        if (!conRepo.existsById(concertId)) {
            throw new ConcertNotFoundException(concertId);
        }

        return ticketRepository.findByQrTokenAndOrderConcertId(qrToken, concertId)
                .orElseThrow(() -> new TicketNotFoundException(qrToken));
    }
}
