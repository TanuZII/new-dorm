package th.ac.dusit.dorm.imports;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import th.ac.dusit.dorm.common.ResourceNotFoundException;
import th.ac.dusit.dorm.documents.DocumentStorage;
import th.ac.dusit.dorm.masterdata.MasterDataService;

@Service
public class MasterDataImportService {
    static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Duration PREVIEW_LIFETIME = Duration.ofHours(1);
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ImportSessionRepository sessions;
    private final ImportErrorRepository errors;
    private final MasterDataWorkbookParser parser;
    private final ImportErrorWorkbookWriter errorWorkbookWriter;
    private final DocumentStorage storage;
    private final MasterDataService masterDataService;
    private final Clock clock;

    @Autowired
    public MasterDataImportService(
            ImportSessionRepository sessions,
            ImportErrorRepository errors,
            MasterDataWorkbookParser parser,
            ImportErrorWorkbookWriter errorWorkbookWriter,
            DocumentStorage storage,
            MasterDataService masterDataService) {
        this(sessions, errors, parser, errorWorkbookWriter, storage, masterDataService, Clock.systemUTC());
    }

    MasterDataImportService(
            ImportSessionRepository sessions,
            ImportErrorRepository errors,
            MasterDataWorkbookParser parser,
            ImportErrorWorkbookWriter errorWorkbookWriter,
            DocumentStorage storage,
            MasterDataService masterDataService,
            Clock clock) {
        this.sessions = sessions;
        this.errors = errors;
        this.parser = parser;
        this.errorWorkbookWriter = errorWorkbookWriter;
        this.storage = storage;
        this.masterDataService = masterDataService;
        this.clock = clock;
    }

    @Transactional
    public ImportPreviewResponse preview(MultipartFile file, String actor) {
        byte[] bytes = validateAndRead(file);
        var parsed = parser.parse(bytes);
        var stored = storage.store("imports", safeName(file.getOriginalFilename()), bytes);
        Instant createdAt = clock.instant();
        Instant expiresAt = createdAt.plus(PREVIEW_LIFETIME);
        String token = UUID.randomUUID().toString();
        int invalidRows = (int) parsed.errors().stream().map(ImportRowError::rowNumber).distinct().count();
        var session = sessions.save(new ImportSessionEntity(
                token,
                safeName(file.getOriginalFilename()),
                stored.path(),
                stored.sha256(),
                invalidRows == 0 ? ImportStatus.READY : ImportStatus.INVALID,
                parsed.totalRows(),
                parsed.totalRows() - invalidRows,
                invalidRows,
                actor,
                createdAt,
                expiresAt));
        errors.saveAll(parsed.errors().stream()
                .map(error -> new ImportErrorEntity(session.getId(), error))
                .toList());
        return new ImportPreviewResponse(
                token, stored.sha256(), parsed.totalRows(), parsed.totalRows() - invalidRows,
                invalidRows, List.copyOf(parsed.errors()), expiresAt);
    }

    @Transactional
    public ImportConfirmResponse confirm(String token, String actor, String ipAddress) {
        var session = sessions.findForUpdateByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Import preview not found"));
        if (session.getStatus() != ImportStatus.READY) {
            throw new IllegalStateException("Import preview is not ready for confirmation");
        }
        if (!session.getExpiresAt().isAfter(clock.instant())) {
            throw new IllegalStateException("Import preview has expired");
        }
        byte[] bytes = storage.read(session.getStoragePath());
        if (!MessageDigest.isEqual(
                session.getSha256().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                sha256(bytes).getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
            throw new IllegalStateException("Import file hash has changed");
        }
        var parsed = parser.parse(bytes);
        if (!parsed.errors().isEmpty()) {
            throw new IllegalStateException("Import file no longer passes validation");
        }
        int imported = masterDataService.importAll(parsed.validItems(), actor, ipAddress);
        session.confirm(clock.instant());
        return new ImportConfirmResponse(token, imported);
    }

    @Transactional(readOnly = true)
    public byte[] errorWorkbook(String token) {
        var session = find(token);
        return errorWorkbookWriter.write(
                errors.findByImportSessionIdOrderByRowNumberAscIdAsc(session.getId()));
    }

    private ImportSessionEntity find(String token) {
        return sessions.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Import preview not found"));
    }

    private byte[] validateAndRead(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("XLSX file is required");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("XLSX file exceeds 10 MB");
        String contentType = file.getContentType();
        if (contentType != null && !XLSX_CONTENT_TYPE.equals(contentType)
                && !MediaTypes.OCTET_STREAM.equals(contentType)) {
            throw new IllegalArgumentException("Only XLSX files are supported");
        }
        try {
            byte[] bytes = file.getBytes();
            if (!isZip(bytes)) throw new IllegalArgumentException("File is not a valid XLSX container");
            return bytes;
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException("Unable to read uploaded file", exception);
        }
    }

    private boolean isZip(byte[] bytes) {
        return bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K'
                && ((bytes[2] == 3 && bytes[3] == 4)
                || (bytes[2] == 5 && bytes[3] == 6)
                || (bytes[2] == 7 && bytes[3] == 8));
    }

    private String safeName(String name) {
        if (name == null || name.isBlank()) return "master-data.xlsx";
        String normalized = name.replace('\\', '/');
        String baseName = normalized.substring(normalized.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_");
        return baseName.substring(0, Math.min(baseName.length(), 255));
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static final class MediaTypes {
        private static final String OCTET_STREAM = "application/octet-stream";
    }
}
