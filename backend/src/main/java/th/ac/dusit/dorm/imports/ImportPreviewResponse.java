package th.ac.dusit.dorm.imports;

import java.time.Instant;
import java.util.List;

public record ImportPreviewResponse(
        String token,
        String sha256,
        int totalRows,
        int validRows,
        int invalidRows,
        List<ImportRowError> errors,
        Instant expiresAt) {
}
