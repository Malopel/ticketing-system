package accordion_symphonic.ticketing.bank;

import accordion_symphonic.ticketing.bank.camt.CamtImportItemResult;
import accordion_symphonic.ticketing.bank.camt.CamtTransaction;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;
import accordion_symphonic.ticketing.payment.exception.OrderCannotBePaidException;
import accordion_symphonic.ticketing.payment.service.PaymentCompletionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BankReconciliationService {

    private static final Pattern ORDER_REFERENCE =
            Pattern.compile(
                    "\\bAS-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}\\b"
            );

    private final OrderRepository orderRepository;

    private final PaymentCompletionService
            paymentCompletionService;

    public BankReconciliationService(
            OrderRepository orderRepository,
            PaymentCompletionService paymentCompletionService
    ) {
        this.orderRepository = orderRepository;
        this.paymentCompletionService =
                paymentCompletionService;
    }

    @Transactional
    public CamtImportItemResult reconcile(
            CamtTransaction transaction
    ) {
        String orderReference =
                extractOrderReference(
                        transaction.searchableText()
                );

        if (orderReference == null) {
            return result(
                    transaction,
                    null,
                    CamtImportItemResult.Status.NO_ORDER_REFERENCE,
                    "Keine Accordion-Symphonic-Bestellnummer gefunden."
            );
        }

        if (!"EUR".equalsIgnoreCase(
                transaction.currency()
        )) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.NON_EUR,
                    "Zahlung ist nicht in EUR."
            );
        }

        Order order =
                orderRepository
                        .findByPublicId(orderReference)
                        .orElse(null);

        if (order == null) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.ORDER_NOT_FOUND,
                    "Keine passende Bestellung gefunden."
            );
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.ALREADY_PAID,
                    "Bestellung ist bereits bezahlt. Zahlung manuell prüfen."
            );
        }

        if (order.getPaymentMethod()
                != PaymentMethod.BANK_TRANSFER) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.WRONG_PAYMENT_METHOD,
                    "Bestellung verwendet keine Banküberweisung."
            );
        }

        if (order.getTotalAmount().compareTo(
                transaction.amount()
        ) != 0) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.AMOUNT_MISMATCH,
                    "Betrag stimmt nicht mit der Bestellung überein."
            );
        }

        if (order.getStatus()
                != OrderStatus.PAYMENT_PENDING) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.ORDER_NOT_PENDING,
                    "Bestellung wartet nicht mehr auf Zahlung."
            );
        }

        if (order.shouldExpirePayment()) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.ORDER_EXPIRED,
                    "Zahlungsfrist ist abgelaufen. Manuelle Prüfung erforderlich."
            );
        }

        try {
            paymentCompletionService
                    .completePayment(order.getId());

            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.PAID,
                    "Zahlung erfolgreich zugeordnet."
            );

        } catch (OrderCannotBePaidException exception) {
            return result(
                    transaction,
                    orderReference,
                    CamtImportItemResult.Status.PAYMENT_REJECTED,
                    "Bestellung konnte nicht automatisch bezahlt werden."
            );
        }
    }

    private String extractOrderReference(
            String text
    ) {
        if (text == null) {
            return null;
        }

        Matcher matcher =
                ORDER_REFERENCE.matcher(
                        text.toUpperCase(Locale.ROOT)
                );

        if (!matcher.find()) {
            return null;
        }

        return matcher.group();
    }

    private CamtImportItemResult result(
            CamtTransaction transaction,
            String orderReference,
            CamtImportItemResult.Status status,
            String message
    ) {
        return new CamtImportItemResult(
                transaction.bankReference(),
                orderReference,
                transaction.amount(),
                status,
                message
        );
    }
}