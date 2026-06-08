package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TicketEmailServiceTest {

    @Test
    void sendTicketsSendEmailToCustomer() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailProperties mailProperties =
                new MailProperties("noreply@accordion-symphonic.local");

        TicketEmailService ticketEmailService = new TicketEmailService(mailSender, mailProperties);

        Concert concert = new Concert(
                "Accordion Night",
                "Ein wohlig warmes Akkordeon Abenteuer, mit den Klängen von aufgedrehtem Chloroform",
                LocalDateTime.now().plusDays(30),
                "Arsch der Welt"
        );

        TicketCategory ticketCategory = new TicketCategory(
                "VIP",
                BigDecimal.valueOf(100),
                50,
                concert
        );

        Order order = new Order(
                concert,
                "kunde@example.com",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                LocalDateTime.now()
        );

        Ticket ticket = new Ticket(
                order,
                ticketCategory
        );

        String qr = ticket.getQrToken();

        ticketEmailService.sendEmail(order, List.of(TicketResponse.fromEntity(ticket)));

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals("noreply@accordion-symphonic.local", message.getFrom());
        assertEquals("kunde@example.com", message.getTo()[0]);
        assertEquals("Your tickets for Accordion Night", message.getSubject());
        assertTrue(message.getText().contains(qr));
    }
}
