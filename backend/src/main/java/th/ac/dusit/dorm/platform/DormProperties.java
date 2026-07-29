package th.ac.dusit.dorm.platform;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "dorm")
public record DormProperties(
        @NotNull Path storagePath,
        @Min(90) int auditRetentionDays,
        @Min(1) @Max(200) int maxPageSize) {
}
