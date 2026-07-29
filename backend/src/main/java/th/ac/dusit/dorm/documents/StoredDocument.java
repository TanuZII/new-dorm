package th.ac.dusit.dorm.documents;

public record StoredDocument(String path, String originalName, String sha256, long size) {
}

