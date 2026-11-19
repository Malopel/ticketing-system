package accordion_symphonic.ticketing.bank.csv;

import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;
import accordion_symphonic.ticketing.payment.service.PaymentCompletionService;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class VolksbankCsvConfirmService {

    private final VolksbankCsvPreviewService previewService;
    private final OrderRepository orderRepository;
    private final PaymentCompletionService paymentCompletionService;
    private final JdbcTemplate jdbcTemplate;

    public VolksbankCsvConfirmService(
            VolksbankCsvPreviewService previewService,
            OrderRepository orderRepository,
            PaymentCompletionService paymentCompletionService,
            JdbcTemplate jdbcTemplate
    ) {
        this.previewService = previewService;
        this.orderRepository = orderRepository;
        this.paymentCompletionService = paymentCompletionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public CsvConfirmResult confirm(
            byte[] bytes,
            String previewHash,
            String approvedReferences,
            String approvedBy
    ) {
        // 1. Datei erneut vollständig einlesen und prüfen
        CsvPreviewResult currentPreview =
                previewService.preview(bytes);

        if (!currentPreview.fileSha256()
                .equalsIgnoreCase(previewHash)) {
            throw conflict(
                    "Die CSV-Datei unterscheidet sich von der Vorschau."
            );
        }

        List<CsvPreviewResult.Item> readyItems =
                currentPreview.items().stream()
                        .filter(item ->
                                item.status() ==
                                        CsvPreviewResult.Status.READY
                        )
                        .sorted(Comparator.comparing(
                                CsvPreviewResult.Item::orderReference
                        ))
                        .toList();

        List<String> readyReferences = readyItems.stream()
                .map(CsvPreviewResult.Item::orderReference)
                .toList();

        List<String> approved = Arrays.stream(
                        approvedReferences.split(",", -1)
                )
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .sorted()
                .toList();

        if (readyReferences.isEmpty()) {
            throw conflict(
                    "Keine zuordenbaren Zahlungen vorhanden."
            );
        }

        if (!readyReferences.equals(approved)) {
            throw conflict(
                    "Die bestätigten Bestellungen stimmen nicht mehr " +
                            "mit der aktuellen CSV-Vorschau überein."
            );
        }

        // 2. Alle betroffenen Bestellungen sperren und erneut prüfen
        List<Order> lockedOrders = new ArrayList<>();

        for (CsvPreviewResult.Item item : readyItems) {
            Order found = orderRepository
                    .findByPublicId(item.orderReference())
                    .orElseThrow(() -> conflict(
                            "Bestellung nicht mehr vorhanden."
                    ));

            Order locked = orderRepository
                    .findByIdForUpdate(found.getId())
                    .orElseThrow(() -> conflict(
                            "Bestellung nicht mehr vorhanden."
                    ));

            if (!locked.getPublicId()
                    .equals(item.orderReference())) {
                throw conflict(
                        "Bestellreferenz stimmt nicht überein."
                );
            }

            if (locked.getStatus() != OrderStatus.PAYMENT_PENDING
                    || locked.getPaymentMethod()
                    != PaymentMethod.BANK_TRANSFER
                    || locked.shouldExpirePayment()
                    || locked.getConcert().getStatus()
                    != ConcertStatus.PUBLISHED
                    || locked.getTotalAmount()
                    .compareTo(item.amount()) != 0) {
                throw conflict(
                        "Bestellung " + item.orderReference() +
                                " ist nicht mehr verbuchbar. " +
                                "Bitte die Vorschau aktualisieren."
                );
            }

            lockedOrders.add(locked);
        }

        // 3. Verbuchen und Nachweis speichern.
        // Alles geschieht in einer einzigen Datenbanktransaktion.
        for (int index = 0; index < readyItems.size(); index++) {
            CsvPreviewResult.Item item = readyItems.get(index);
            Order order = lockedOrders.get(index);

            jdbcTemplate.update("""
                    INSERT INTO bank_csv_payment_match
                        (order_id, file_sha256, amount, approved_by)
                    VALUES (?, ?, ?, ?)
                    """,
                    order.getId(),
                    currentPreview.fileSha256(),
                    item.amount(),
                    approvedBy
            );

            paymentCompletionService.completePayment(
                    order.getId()
            );
        }

        return new CsvConfirmResult(
                lockedOrders.size(),
                readyReferences
        );
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                message
        );
    }
}
