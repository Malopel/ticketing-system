package accordion_symphonic.ticketing.bank.camt;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/admin/bank-statements")
public class CamtImportController {

    private final CamtImportService camtImportService;

    public CamtImportController(
            CamtImportService camtImportService
    ) {
        this.camtImportService = camtImportService;
    }

    @PostMapping(
            value = "/camt",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CamtImportResult importCamt(
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CAMT-Datei ist leer."
            );
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            return camtImportService.importStatement(
                    inputStream
            );

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CAMT-Datei konnte nicht gelesen werden.",
                    exception
            );
        }
    }
}