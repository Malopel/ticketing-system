package accordion_symphonic.ticketing.bank.csv;

import accordion_symphonic.ticketing.concert.Concert;
import accordion_symphonic.ticketing.concert.ConcertRepository;
import accordion_symphonic.ticketing.mail.TicketEmailService;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderItem;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;
import accordion_symphonic.ticketing.ticket.TicketRepository;
import accordion_symphonic.ticketing.ticketcategory.TicketCategory;
import accordion_symphonic.ticketing.ticketcategory.TicketCategoryRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Prüft den echten CSV-Vorschau- und Verbuchungsweg mit PostgreSQL.
 * Testcontainers verwendet eine eigene, isolierte Datenbank.
 * Der E-Mail-Service ist gemockt: Es werden keine echten Mails verschickt.
 */
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
        "ticketing.security.admin.username=admin",
        "ticketing.security.admin.password=test-password",
        "ticketing.payment.webhook-secret=test-webhook-secret",
        "ticketing.payment.provider=fake",
        "ticketing.bank-transfer.enabled=true",
        "ticketing.bank-transfer.recipient=Testverein",
        "ticketing.bank-transfer.iban=DE00000000000000000000",
        "ticketing.bank-transfer.bic=TESTDE00XXX",
        "ticketing.bank-transfer.reservation-hours=72"
})
class VolksbankCsvConfirmIntegrationTest {

    private static final String TEST_IBAN = "DE00000000000000000000";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private VolksbankCsvPreviewService previewService;

    @Autowired
    private VolksbankCsvConfirmService confirmService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private TicketEmailService ticketEmailService;

    @Test
    void validCsvPaymentCreatesTicketsOnceAndStoresAuditEntry() {
        Order order = bankTransferOrder("12.50", 2, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(row(TEST_IBAN, "25,00", "EUR", "Ticket; " + order.getPublicId()));

        CsvPreviewResult preview = previewService.preview(csv);

        assertEquals(1, preview.rowsRead());
        assertEquals(0, preview.ignored());
        assertEquals(1, preview.ready());
        assertEquals(CsvPreviewResult.Status.READY, preview.items().getFirst().status());
        assertEquals(OrderStatus.PAYMENT_PENDING, reloaded(order).getStatus());
        assertTrue(ticketRepository.findByOrderId(order.getId()).isEmpty());

        CsvConfirmResult result = confirmService.confirm(
                csv, preview.fileSha256(), order.getPublicId(), "test-admin"
        );

        assertEquals(1, result.paid());
        assertEquals(List.of(order.getPublicId()), result.orderReferences());
        assertEquals(OrderStatus.PAID, reloaded(order).getStatus());
        assertEquals(2, ticketRepository.findByOrderId(order.getId()).size());
        assertEquals(1, auditCount(order));
        assertEquals("test-admin", jdbcTemplate.queryForObject(
                "SELECT approved_by FROM bank_csv_payment_match WHERE order_id = ?",
                String.class,
                order.getId()
        ));
        verify(ticketEmailService, times(1)).sendEmail(any(Order.class), anyList());
    }

    @Test
    void importingSameFileAgainDoesNotGenerateMoreTickets() {
        Order order = bankTransferOrder("10.00", 2, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(row(TEST_IBAN, "20,00", "EUR", order.getPublicId()));
        CsvPreviewResult firstPreview = previewService.preview(csv);
        confirmService.confirm(csv, firstPreview.fileSha256(), order.getPublicId(), "admin");

        CsvPreviewResult secondPreview = previewService.preview(csv);
        assertEquals(0, secondPreview.ready());
        assertEquals(CsvPreviewResult.Status.ALREADY_PAID,
                secondPreview.items().getFirst().status());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> confirmService.confirm(
                        csv, firstPreview.fileSha256(), order.getPublicId(), "admin"
                ));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(OrderStatus.PAID, reloaded(order).getStatus());
        assertEquals(2, ticketRepository.findByOrderId(order.getId()).size());
        assertEquals(1, auditCount(order));
        verify(ticketEmailService, times(1)).sendEmail(any(Order.class), anyList());
    }

    @Test
    void incorrectAmountIsNotEligibleAndCannotBeConfirmed() {
        Order order = bankTransferOrder("25.00", 1, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(row(TEST_IBAN, "24,99", "EUR", order.getPublicId()));

        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(0, preview.ready());
        assertEquals(CsvPreviewResult.Status.AMOUNT_MISMATCH,
                preview.items().getFirst().status());
        assertThrows(ResponseStatusException.class,
                () -> confirmService.confirm(
                        csv, preview.fileSha256(), order.getPublicId(), "admin"
                ));

        assertUnpaidWithoutTickets(order);
    }

    @Test
    void wrongBankAccountRejectsEntireFile() {
        Order order = bankTransferOrder("10.00", 1, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(row("DE99999999999999999999", "10,00", "EUR", order.getPublicId()));

        assertThrows(IllegalArgumentException.class, () -> previewService.preview(csv));
        assertUnpaidWithoutTickets(order);
    }

    @Test
    void twoCreditsForSameOrderRequireManualReview() {
        Order order = bankTransferOrder("20.00", 1, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(
                row(TEST_IBAN, "20,00", "EUR", order.getPublicId()),
                row(TEST_IBAN, "20,00", "EUR", "Nochmal " + order.getPublicId())
        );

        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(0, preview.ready());
        assertEquals(2, preview.reviewRequired());
        assertTrue(preview.items().stream().allMatch(
                item -> item.status() == CsvPreviewResult.Status.DUPLICATE_REFERENCE
        ));
        assertUnpaidWithoutTickets(order);
    }

    @Test
    void expiredBankTransferCannotBeConfirmed() {
        Order order = bankTransferOrder("10.00", 1, LocalDateTime.now().minusMinutes(1));
        byte[] csv = csv(row(TEST_IBAN, "10,00", "EUR", order.getPublicId()));

        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(CsvPreviewResult.Status.ORDER_EXPIRED,
                preview.items().getFirst().status());
        assertEquals(0, preview.ready());
        assertThrows(ResponseStatusException.class,
                () -> confirmService.confirm(
                        csv, preview.fileSha256(), order.getPublicId(), "admin"
                ));
        assertUnpaidWithoutTickets(order);
    }

    @Test
    void changedCsvAfterPreviewIsRejected() {
        Order order = bankTransferOrder("10.00", 1, LocalDateTime.now().plusHours(3));
        byte[] original = csv(row(TEST_IBAN, "10,00", "EUR", order.getPublicId()));
        byte[] changed = csv(row(TEST_IBAN, "10,00", "EUR", "Geändert " + order.getPublicId()));
        CsvPreviewResult preview = previewService.preview(original);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> confirmService.confirm(
                        changed, preview.fileSha256(), order.getPublicId(), "admin"
                ));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertUnpaidWithoutTickets(order);
    }

    @Test
    void orderChangedAfterPreviewCannotBePaid() {
        Order order = bankTransferOrder("10.00", 1, LocalDateTime.now().plusHours(3));
        byte[] csv = csv(row(TEST_IBAN, "10,00", "EUR", order.getPublicId()));
        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(1, preview.ready());

        // Simuliert eine konkurrierende Statusänderung zwischen Vorschau
        // und Bestätigung, ohne den Produktivserver zu berühren.
        Order changedOrder = reloaded(order);
        changedOrder.expire();
        orderRepository.saveAndFlush(changedOrder);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> confirmService.confirm(
                        csv, preview.fileSha256(), order.getPublicId(), "admin"
                ));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(OrderStatus.EXPIRED, reloaded(order).getStatus());
        assertTrue(ticketRepository.findByOrderId(order.getId()).isEmpty());
        assertEquals(0, auditCount(order));
    }

    @Test
    void unrelatedCreditIsIgnored() {
        byte[] csv = csv(row(TEST_IBAN, "20,00", "EUR", "Spende für den Verein"));

        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(1, preview.rowsRead());
        assertEquals(1, preview.ignored());
        assertEquals(0, preview.ready());
        assertTrue(preview.items().isEmpty());
    }

    @Test
    void batchFailureRollsBackEarlierPaymentAndTicketCreation() {
        Order first = bankTransferOrder("10.00", 2, LocalDateTime.now().plusHours(3));
        Order second = bankTransferOrder("15.00", 1, LocalDateTime.now().plusHours(3));

        List<Order> sorted = Stream.of(first, second)
                .sorted(Comparator.comparing(Order::getPublicId))
                .toList();
        Order firstProcessed = sorted.get(0);
        Order secondProcessed = sorted.get(1);

        // Absichtlich inkonsistenter Testdatensatz: Der zweite Auftrag hat schon
        // einen Audit-Eintrag, ist aber noch PAYMENT_PENDING. Der zweite INSERT
        // muss deshalb an der UNIQUE(order_id)-Constraint scheitern.
        jdbcTemplate.update("""
                        INSERT INTO bank_csv_payment_match
                            (order_id, file_sha256, amount, approved_by)
                        VALUES (?, ?, ?, ?)
                        """,
                secondProcessed.getId(), "prior-test-entry",
                secondProcessed.getTotalAmount(), "admin");

        byte[] csv = csv(
                row(TEST_IBAN, german(first.getTotalAmount()), "EUR", first.getPublicId()),
                row(TEST_IBAN, german(second.getTotalAmount()), "EUR", second.getPublicId())
        );

        CsvPreviewResult preview = previewService.preview(csv);
        assertEquals(2, preview.ready());
        String approved = sorted.stream().map(Order::getPublicId)
                .collect(Collectors.joining(","));

        assertThrows(DataIntegrityViolationException.class,
                () -> confirmService.confirm(csv, preview.fileSha256(), approved, "admin"));

        assertUnpaidWithoutTickets(firstProcessed);
        assertEquals(OrderStatus.PAYMENT_PENDING,
                reloaded(secondProcessed).getStatus());
        assertTrue(ticketRepository.findByOrderId(secondProcessed.getId()).isEmpty());
        assertEquals(0, auditCount(firstProcessed));
        assertEquals(1, auditCount(secondProcessed));
        verify(ticketEmailService, never()).sendEmail(any(Order.class), anyList());
    }

    private Order bankTransferOrder(
            String unitPrice,
            int quantity,
            LocalDateTime paymentExpiresAt
    ) {
        Concert concert = new Concert(
                "CSV-Integrationstest", "Nur Testdaten",
                LocalDateTime.now().plusDays(30), "Testsaal"
        );
        concert.publish();
        concert = concertRepository.saveAndFlush(concert);

        TicketCategory category = ticketCategoryRepository.saveAndFlush(
                new TicketCategory(
                        "Normalpreis", new BigDecimal(unitPrice), 100, concert
                )
        );

        String random = UUID.randomUUID().toString().replace("-", "")
                .toUpperCase();
        String publicId = "AS-" + random.substring(0, 4) + "-"
                + random.substring(4, 8) + "-" + random.substring(8, 12);
        String tokenHash = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        Order order = new Order(
                concert, "integrationstest@example.org", tokenHash,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), publicId
        );
        order.addItem(new OrderItem(category, quantity, new BigDecimal(unitPrice)));
        order.markAsPaymentPending(PaymentMethod.BANK_TRANSFER, paymentExpiresAt);
        return orderRepository.saveAndFlush(order);
    }

    private Order reloaded(Order order) {
        return orderRepository.findById(order.getId()).orElseThrow();
    }

    private void assertUnpaidWithoutTickets(Order order) {
        assertEquals(OrderStatus.PAYMENT_PENDING, reloaded(order).getStatus());
        assertTrue(ticketRepository.findByOrderId(order.getId()).isEmpty());
        assertEquals(0, auditCount(order));
    }

    private int auditCount(Order order) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM bank_csv_payment_match WHERE order_id = ?",
                Integer.class, order.getId()
        );
        return count == null ? 0 : count;
    }

    private static byte[] csv(String... rows) {
        return ("IBAN Auftragskonto;Buchungstag;Betrag;Waehrung;Verwendungszweck\n"
                + String.join("\n", rows) + "\n")
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String row(String iban, String amount, String currency, String purpose) {
        return Stream.of(
                        iban, LocalDate.now().format(DATE_FORMAT), amount, currency, purpose
                )
                .map(VolksbankCsvConfirmIntegrationTest::csvEscape)
                .collect(Collectors.joining(";"));
    }

    private static String csvEscape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String german(BigDecimal amount) {
        return amount.setScale(2).toPlainString().replace('.', ',');
    }
}
