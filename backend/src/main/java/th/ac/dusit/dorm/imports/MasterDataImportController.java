package th.ac.dusit.dorm.imports;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/imports")
public class MasterDataImportController {
    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final MasterDataImportService service;

    public MasterDataImportController(MasterDataImportService service) {
        this.service = service;
    }

    @PostMapping(value = "/master-data/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportPreviewResponse preview(@RequestPart("file") MultipartFile file, Principal principal) {
        return service.preview(file, principal.getName());
    }

    @PostMapping("/{token}/confirm")
    public ImportConfirmResponse confirm(
            @PathVariable String token, Principal principal, HttpServletRequest request) {
        return service.confirm(token, principal.getName(), request.getRemoteAddr());
    }

    @GetMapping("/{token}/errors.xlsx")
    public ResponseEntity<byte[]> errors(@PathVariable String token) {
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("import-errors.xlsx").build().toString())
                .body(service.errorWorkbook(token));
    }
}
