package th.ac.dusit.dorm.common;

public final class DomainConflictException extends RuntimeException {
    private final String code;

    public DomainConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
