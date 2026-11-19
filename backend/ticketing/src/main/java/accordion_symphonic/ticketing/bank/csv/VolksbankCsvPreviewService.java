
package accordion_symphonic.ticketing.bank.csv;

import accordion_symphonic.ticketing.bank.dto.BankTransferProperties;
import accordion_symphonic.ticketing.concert.ConcertStatus;
import accordion_symphonic.ticketing.order.Order;
import accordion_symphonic.ticketing.order.OrderRepository;
import accordion_symphonic.ticketing.order.OrderStatus;
import accordion_symphonic.ticketing.order.PaymentMethod;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static accordion_symphonic.ticketing.bank.csv.CsvPreviewResult.Status.*;

@Service
public class VolksbankCsvPreviewService {

    private static final Pattern ORDER_REFERENCE =
            Pattern.compile(
                    "(?<![A-Z0-9])AS-[A-F0-9]{4}-[A-F0-9]{4}-[A-F0-9]{4}(?![A-Z0-9])"
            );

    private static final Pattern GERMAN_AMOUNT =
            Pattern.compile(
                    "[+-]?(?:\\d{1,3}(?:\\.\\d{3})+|\\d+)(?:,\\d{1,2})?"
            );

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private static final List<String> REQUIRED_COLUMNS = List.of(
            "IBAN Auftragskonto",
            "Buchungstag",
            "Betrag",
            "Waehrung",
            "Verwendungszweck"
    );

    private final OrderRepository orderRepository;
    private final BankTransferProperties bankProperties;

    public VolksbankCsvPreviewService(
            OrderRepository orderRepository,
            BankTransferProperties bankProperties
    ) {
        this.orderRepository = orderRepository;
        this.bankProperties = bankProperties;
    }

    private record BankRow(
            long rowNumber,
            LocalDate bookingDate,
            BigDecimal amount,
            String currency,
            List<String> references
    ) {}

    @Transactional(readOnly = true)
    public CsvPreviewResult preview(byte[] bytes) {
        if (bankProperties.iban() == null ||
                bankProperties.iban().isBlank()) {
            throw new IllegalStateException(
                    "Vereinskonto ist nicht konfiguriert."
            );
        }

        List<BankRow> rows = parseCsv(bytes);

        // Mehrere Buchungen mit derselben Bestellnummer
        // dürfen nicht automatisch freigegeben werden.
        Map<String, Integer> referenceCounts = new HashMap<>();

        for (BankRow row : rows) {
            for (String reference : row.references()) {
                referenceCounts.merge(reference, 1, Integer::sum);
            }
        }

        List<CsvPreviewResult.Item> items = new ArrayList<>();
        int ignored = 0;

        for (BankRow row : rows) {
            if (row.references().isEmpty()) {
                ignored++;
                continue;
            }

            items.add(checkRow(row, referenceCounts));
        }

        int ready = (int) items.stream()
                .filter(item -> item.status() == READY)
                .count();

        return new CsvPreviewResult(
                rows.size(),
                ignored,
                ready,
                items.size() - ready,
                items
        );
    }

    private List<BankRow> parseCsv(byte[] bytes) {
        String content = decode(bytes);

        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (CSVParser parser =
                     format.parse(new StringReader(content))) {

            for (String column : REQUIRED_COLUMNS) {
                if (!parser.getHeaderMap().containsKey(column)) {
                    throw new IllegalArgumentException(
                            "CSV-Spalte fehlt: " + column
                    );
                }
            }

            List<BankRow> rows = new ArrayList<>();

            for (CSVRecord record : parser) {
                if (rows.size() >= 5000) {
                    throw new IllegalArgumentException(
                            "CSV enthält zu viele Buchungen."
                    );
                }

                try {
                    String expectedIban =
                            normalizeIban(bankProperties.iban());

                    String actualIban = normalizeIban(
                            record.get("IBAN Auftragskonto")
                    );

                    if (!expectedIban.equals(actualIban)) {
                        throw new IllegalArgumentException(
                                "Der Export gehört nicht zum konfigurierten Vereinskonto."
                        );
                    }

                    rows.add(new BankRow(
                            record.getRecordNumber() + 1,
                            LocalDate.parse(
                                    record.get("Buchungstag").trim(),
                                    DATE_FORMAT
                            ),
                            parseAmount(record.get("Betrag")),
                            record.get("Waehrung").trim(),
                            extractReferences(
                                    record.get("Verwendungszweck")
                            )
                    ));

                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException(
                            "CSV-Datensatz " +
                                    record.getRecordNumber() +
                                    " konnte nicht geprüft werden.",
                            exception
                    );
                }
            }

            return rows;

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "CSV-Datei konnte nicht gelesen werden.",
                    exception
            );
        }
    }

    private CsvPreviewResult.Item checkRow(
            BankRow row,
            Map<String, Integer> referenceCounts
    ) {
        if (row.references().size() != 1) {
            return item(
                    row, null, MULTIPLE_REFERENCES,
                    "Mehrere Bestellnummern in einer Buchung."
            );
        }

        String reference = row.references().getFirst();

        if (referenceCounts.getOrDefault(reference, 0) > 1) {
            return item(
                    row, reference, DUPLICATE_REFERENCE,
                    "Bestellnummer kommt mehrfach im Export vor."
            );
        }

        if (!"EUR".equalsIgnoreCase(row.currency())) {
            return item(
                    row, reference, NON_EUR,
                    "Buchung ist nicht in EUR."
            );
        }

        if (row.amount().signum() <= 0) {
            return item(
                    row, reference, NOT_CREDIT,
                    "Keine positive Gutschrift."
            );
        }

        Order order = orderRepository.findByPublicId(reference)
                .orElse(null);

        if (order == null) {
            return item(
                    row, reference, ORDER_NOT_FOUND,
                    "Keine passende Bestellung gefunden."
            );
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return item(
                    row, reference, ALREADY_PAID,
                    "Bestellung ist bereits bezahlt. Bitte prüfen."
            );
        }

        if (order.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            return item(
                    row, reference, WRONG_METHOD,
                    "Bestellung verwendet keine Banküberweisung."
            );
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return item(
                    row, reference, NOT_PENDING,
                    "Bestellung wartet nicht mehr auf Zahlung."
            );
        }

        if (order.shouldExpirePayment()) {
            return item(
                    row, reference, ORDER_EXPIRED,
                    "Zahlungsfrist ist abgelaufen."
            );
        }

        if (order.getConcert().getStatus() != ConcertStatus.PUBLISHED) {
            return item(
                    row, reference, CONCERT_UNAVAILABLE,
                    "Konzert ist nicht mehr veröffentlicht."
            );
        }

        if (order.getTotalAmount().compareTo(row.amount()) != 0) {
            return item(
                    row, reference, AMOUNT_MISMATCH,
                    "Betrag stimmt nicht überein."
            );
        }

        return item(
                row, reference, READY,
                "Zahlung kann zugeordnet werden."
        );
    }

    private CsvPreviewResult.Item item(
            BankRow row,
            String reference,
            CsvPreviewResult.Status status,
            String message
    ) {
        return new CsvPreviewResult.Item(
                row.rowNumber(),
                row.bookingDate(),
                reference,
                row.amount(),
                status,
                message
        );
    }

    private List<String> extractReferences(String purpose) {
        if (purpose == null) {
            return List.of();
        }

        Matcher matcher = ORDER_REFERENCE.matcher(
                purpose.toUpperCase(Locale.ROOT)
        );

        Set<String> references = new LinkedHashSet<>();

        while (matcher.find()) {
            references.add(matcher.group());
        }

        return List.copyOf(references);
    }

    private BigDecimal parseAmount(String value) {
        String cleaned = value.trim()
                .replace("\u00A0", "")
                .replace(" ", "");

        if (!GERMAN_AMOUNT.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Ungültiges deutsches Betragsformat."
            );
        }

        return new BigDecimal(
                cleaned.replace(".", "").replace(",", ".")
        );
    }

    private String normalizeIban(String value) {
        return value.replaceAll("\\s+", "")
                .toUpperCase(Locale.ROOT);
    }

    private String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();

        } catch (CharacterCodingException exception) {
            // Häufig bei deutschen Bank-CSV-Exporten
            return new String(
                    bytes,
                    Charset.forName("windows-1252")
            );
        }
    }
}
