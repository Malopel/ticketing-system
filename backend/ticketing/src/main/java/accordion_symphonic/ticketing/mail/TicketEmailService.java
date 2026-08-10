package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.TicketPdfService;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TicketEmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final TicketPdfService ticketPdfService;

    public TicketEmailService(
            JavaMailSender mailSender,
            MailProperties mailProperties,
            TicketPdfService ticketPdfService
    ) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
        this.ticketPdfService = ticketPdfService;
    }

    public void sendEmail(Order order, List<TicketResponse> tickets) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(mailProperties.from());
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Your tickets for " + order.getConcert().getTitle());
            helper.setText(buildText(order, tickets), false);

            byte[] ticketPdf = ticketPdfService.createTicketPdf(order, tickets);

            helper.addAttachment(
                    buildAttachmentFilename(order),
                    new ByteArrayResource(ticketPdf),
                    MediaType.APPLICATION_PDF_VALUE
            );

            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new IllegalStateException("Ticket-E-Mail konnte nicht versendet werden.", exception);
        }
    }

    private String buildText(Order order, List<TicketResponse> tickets) {
        StringBuilder text = new StringBuilder();

        text.append("Vielen Dank für Ihre Bestellung.\n\n");
        text.append("Ihre Tickets für ");
        text.append(order.getConcert().getTitle());
        text.append(" finden Sie im PDF-Anhang.\n\n");

        text.append("Enthaltene Tickets:\n\n");

        for (TicketResponse ticket : tickets) {
            text.append("- Ticket-ID: ").append(ticket.id()).append("\n");
            text.append("  Kategorie: ").append(ticket.ticketCategoryName()).append("\n");
        }

        text.append("\nBitte bringen Sie die Ticket-PDFs oder die QR-Codes zum Einlass mit.\n\n");
        text.append("Denken Sie bitte daran, im Kongresszentrum ist ausschließlich Kartenzahlung erlaubt.");

        return text.toString();
    }

    private String buildAttachmentFilename(Order order) {
        if (order.getId() == null) {
            return "tickets.pdf";
        }

        return "tickets-order-" + order.getId() + ".pdf";
    }
}