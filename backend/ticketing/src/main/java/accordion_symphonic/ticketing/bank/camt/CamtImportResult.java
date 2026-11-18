package accordion_symphonic.ticketing.bank.camt;

import java.util.List;

public record CamtImportResult(
        int transactionsFound,
        int paid,
        int reviewRequired,
        List<CamtImportItemResult> items
) {
}