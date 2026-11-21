
package accordion_symphonic.ticketing.mail;

import accordion_symphonic.ticketing.bank.BankTransferStartedEvent;
import accordion_symphonic.ticketing.bank.dto.BankTransferProperties;

import jakarta.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
public class OrderConfirmationEmailListener {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy 'um' HH:mm"
            );

    private final JavaMailSender mailSender;
    private final BankTransferProperties bankProperties;
    private final String fromAddress;
    private final String fromName;

    public OrderConfirmationEmailListener(
            JavaMailSender mailSender,
            BankTransferProperties bankProperties,
            @Value("${ticketing.mail.from}") String fromAddress,
            @Value("${ticketing.mail.from-name:ReiMa Konzertorchester}")
            String fromName
    ) {
        this.mailSender = mailSender;
        this.bankProperties = bankProperties;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @Async("mailExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(BankTransferStartedEvent event) {
        try {
            String amount = String.format(
                    Locale.GERMANY,
                    "%.2f €",
                    event.amount()
            );

            String body = """
                    Hallo,

                    vielen Dank für deine Bestellung!

                    Wir haben deine Bestellung erfolgreich erhalten.
                    Sobald der Zahlungseingang bestätigt wurde,
                    erhältst du deine Tickets per E-Mail.

                    DEINE BESTELLUNG

                    Konzert: %s
                    Bestellnummer: %s
                    Gesamtbetrag: %s

                    ZAHLUNGSINFORMATIONEN

                    Empfänger: %s
                    IBAN: %s
                    Verwendungszweck: %s
                    Zahlungsfrist: %s

                    Bitte verwende den angegebenen
                    Verwendungszweck unverändert und nutze bitte Echtzeitüberweisung.

                    Wir freuen uns bereits heute auf einen
                    tollen Konzertabend mit dir!

                    Musikalische Grüße
                    Dein ReiMa Konzertorchester

                    Hinweis: Die Zahlung ist noch ausstehend.
                    Die Tickets folgen nach bestätigtem
                    Zahlungseingang.
                    """.formatted(
                    event.concertTitle(),
                    event.publicId(),
                    amount,
                    bankProperties.recipient(),
                    bankProperties.iban(),
                    event.publicId(),
                    event.paymentExpiresAt().format(DATE_FORMAT)
            );

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(fromAddress, fromName);
            helper.setTo(event.customerEmail());
            helper.setSubject(
                    "Deine Bestellbestätigung – " +
                            event.publicId()
            );
            helper.setText(body, false);

            mailSender.send(message);

            log.info(
                    "Bestellbestätigung für Order {} versendet.",
                    event.orderId()
            );

        } catch (Exception exception) {
            log.error(
                    "Bestellbestätigung für Order {} " +
                            "konnte nicht versendet werden.",
                    event.orderId(),
                    exception
            );
        }
    }
}