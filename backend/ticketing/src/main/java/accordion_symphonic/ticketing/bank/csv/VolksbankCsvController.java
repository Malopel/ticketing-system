
package accordion_symphonic.ticketing.bank.csv;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/bank-statements")
public class VolksbankCsvController {

    private static final long MAX_FILE_SIZE = 2_000_000;

    private final VolksbankCsvPreviewService previewService;

    public VolksbankCsvController(
            VolksbankCsvPreviewService previewService
    ) {
        this.previewService = previewService;
    }

    @PostMapping(
            value = "/csv/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CsvPreviewResult preview(
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CSV-Datei ist leer oder zu groß."
            );
        }

        try {
            return previewService.preview(file.getBytes());

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ungültiger Volksbank-CSV-Export.",
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CSV-Datei konnte nicht gelesen werden.",
                    exception
            );
        }
    }
}
