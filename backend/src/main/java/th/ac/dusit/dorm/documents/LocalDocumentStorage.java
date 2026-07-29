package th.ac.dusit.dorm.documents;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalDocumentStorage implements DocumentStorage {
    private static final Set<String> CATEGORIES = Set.of(
            "contracts", "payments", "receipts", "deposits", "maintenance", "imports");
    private final Path root;

    @Autowired
    public LocalDocumentStorage(@Value("${dorm.storage-path:./storage}") String root) {
        this(Path.of(root));
    }

    LocalDocumentStorage(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    @Override
    public StoredDocument store(String category, String originalName, byte[] content) {
        if (!CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid document category");
        }
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Document content is required");
        }
        String extension = extensionOf(originalName);
        String generatedName = UUID.randomUUID() + extension;
        Path categoryDirectory = root.resolve(category).normalize();
        Path target = categoryDirectory.resolve(generatedName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid document path");
        }
        try {
            Files.createDirectories(categoryDirectory);
            Files.write(target, content);
            return new StoredDocument(
                    root.relativize(target).toString().replace('\\', '/'),
                    originalName,
                    sha256(content),
                    content.length);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store document", exception);
        }
    }

    private String extensionOf(String originalName) {
        if (originalName == null) return "";
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) return "";
        String extension = originalName.substring(dot).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,8}") ? extension : "";
    }

    private String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
