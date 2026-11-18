package accordion_symphonic.ticketing.bank.camt;

import accordion_symphonic.ticketing.bank.BankReconciliationService;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class CamtImportService {

    private final Camt053Parser parser;
    private final BankReconciliationService reconciliationService;

    public CamtImportService(
            Camt053Parser parser,
            BankReconciliationService reconciliationService
    ) {
        this.parser = parser;
        this.reconciliationService =
                reconciliationService;
    }

    public CamtImportResult importStatement(
            InputStream inputStream
    ) {
        List<CamtTransaction> transactions =
                parser.parse(inputStream);

        List<CamtImportItemResult> results =
                new ArrayList<>();

        for (CamtTransaction transaction : transactions) {
            results.add(
                    reconciliationService.reconcile(
                            transaction
                    )
            );
        }

        int paid =
                (int) results.stream()
                        .filter(result ->
                                result.status()
                                        == CamtImportItemResult.Status.PAID
                        )
                        .count();

        return new CamtImportResult(
                transactions.size(),
                paid,
                results.size() - paid,
                results
        );
    }
}