package th.ac.dusit.dorm.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class DormPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsOperationalSettingsToValidatedTypedProperties() {
        contextRunner
                .withPropertyValues(
                        "dorm.storage-path=./documents",
                        "dorm.audit-retention-days=365",
                        "dorm.max-page-size=200")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var properties = context.getBean(DormProperties.class);
                    assertThat(properties.storagePath()).isEqualTo(Path.of("documents"));
                    assertThat(properties.auditRetentionDays()).isEqualTo(365);
                    assertThat(properties.maxPageSize()).isEqualTo(200);
                });
    }

    @Test
    void rejectsAuditRetentionBelowTorMinimum() {
        contextRunner
                .withPropertyValues(
                        "dorm.storage-path=./documents",
                        "dorm.audit-retention-days=89",
                        "dorm.max-page-size=200")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(DormProperties.class)
    static class PropertiesConfiguration {
    }
}
