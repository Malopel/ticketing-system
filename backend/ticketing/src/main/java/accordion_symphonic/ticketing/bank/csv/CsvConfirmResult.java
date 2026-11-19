package accordion_symphonic.ticketing.bank.csv;

import java.util.List;

public record CsvConfirmResult(
        int paid,
        List<String> orderReferences
) {}