package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.ticket.TicketResponse;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketEmailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public TicketEmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void sendEmail(Order order, List<TicketResponse> tickets) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailProperties.from());
        message.setTo(order.getCustomerEmail());
        message.setSubject("Your tickets for " + order.getConcert().getTitle());
        message.setText(buildText(order, tickets));

        mailSender.send(message);
    }

    private String buildText(Order order, List<TicketResponse> tickets) {
        StringBuilder text = new StringBuilder();

        text.append("Thank you for your order Diggah.\n\n");
        text.append("Hier deine Tickets LAN:\n\n");

        for (TicketResponse ticket : tickets) {
            text.append("- Ticket ID: ").append(ticket.id()).append("\n");
            text.append("  QR Token: ").append(ticket.qrToken()).append("\n");
        }

        return text.toString();
    }
}
