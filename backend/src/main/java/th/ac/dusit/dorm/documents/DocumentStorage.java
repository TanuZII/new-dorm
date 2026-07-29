package th.ac.dusit.dorm.documents;

public interface DocumentStorage {
    StoredDocument store(String category, String originalName, byte[] content);

    byte[] read(String path);
}
