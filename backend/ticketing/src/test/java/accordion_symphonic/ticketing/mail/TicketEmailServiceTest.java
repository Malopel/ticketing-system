package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.Ticket;
import accordion_symphonic.ticketing.ticket.TicketPdfService;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketEmailServiceTest {

    @Test
    void sendTicketsSendsEmailWithPdfAttachmentToCustomer() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        TicketPdfService ticketPdfService = mock(TicketPdfService.class);

        MailProperties mailProperties =
                new MailProperties("noreply@accordion-symphonic.local");

        MimeMessage mimeMessage = new MimeMessage(
                Session.getInstance(new Properties())
        );

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        TicketEmailService ticketEmailService = new TicketEmailService(
                mailSender,
                mailProperties,
                ticketPdfService
        );

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

        Ticket ticket = new Ticket(order, ticketCategory);
        List<TicketResponse> tickets = List.of(TicketResponse.fromEntity(ticket));

        byte[] pdfBytes = "%PDF-test".getBytes();

        when(ticketPdfService.createTicketPdf(order, tickets))
                .thenReturn(pdfBytes);

        ticketEmailService.sendEmail(order, tickets);

        ArgumentCaptor<MimeMessage> messageCaptor =
                ArgumentCaptor.forClass(MimeMessage.class);

        verify(mailSender).send(messageCaptor.capture());
        verify(ticketPdfService).createTicketPdf(order, tickets);

        MimeMessage message = messageCaptor.getValue();

        assertEquals(
                "noreply@accordion-symphonic.local",
                message.getFrom()[0].toString()
        );

        assertEquals(
                "kunde@example.com",
                message.getRecipients(Message.RecipientType.TO)[0].toString()
        );

        assertEquals(
                "Your tickets for Accordion Night",
                message.getSubject()
        );

        assertTrue(message.getContent() instanceof Multipart);

        Multipart multipart = (Multipart) message.getContent();

        assertTrue(
                multipartContainsText(multipart, "Ihre Tickets für Accordion Night finden Sie im PDF-Anhang."),
                "Mailtext wurde im Multipart nicht gefunden."
        );

        assertTrue(
                multipartContainsPdfAttachment(multipart),
                "PDF-Anhang wurde im Multipart nicht gefunden."
        );
    }

    private boolean multipartContainsText(Multipart multipart, String expectedText) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            Object content = bodyPart.getContent();

            if (content instanceof String text && text.contains(expectedText)) {
                return true;
            }

            if (content instanceof Multipart nestedMultipart
                    && multipartContainsText(nestedMultipart, expectedText)) {
                return true;
            }
        }

        return false;
    }

    private boolean multipartContainsPdfAttachment(Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            Object content = bodyPart.getContent();

            boolean hasPdfFilename =
                    bodyPart.getFileName() != null
                            && bodyPart.getFileName().toLowerCase().endsWith(".pdf");

            boolean hasPdfContentType =
                    bodyPart.getContentType() != null
                            && bodyPart.getContentType().toLowerCase().contains("application/pdf");

            if (hasPdfFilename || hasPdfContentType) {
                return true;
            }

            if (content instanceof Multipart nestedMultipart
                    && multipartContainsPdfAttachment(nestedMultipart)) {
                return true;
            }
        }

        return false;
    }
}