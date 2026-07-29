package th.ac.dusit.dorm.imports;

public record ImportRowError(
        int rowNumber,
        String field,
        String rejectedValue,
        String code,
        String message) {
}
