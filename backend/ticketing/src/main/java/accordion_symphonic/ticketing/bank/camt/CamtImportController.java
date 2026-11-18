package accordion_symphonic.ticketing.bank.camt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
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
            log.error(
                "CAMT-Import fehlgeschlagen",
                exception
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "CAMT-Datei konnte nicht verarbeitet werden.",
                exception
            );

        } catch (IOException exception) {
            log.error(
                "CAMT-Datei konnte nicht gelesen werden",
                exception
            );

            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "CAMT-Datei konnte nicht gelesen werden.",
                exception
            );
        }
    }
}