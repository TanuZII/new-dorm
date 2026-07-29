package th.ac.dusit.dorm.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayRuntimeTest {

    @Test
    void includesSpringBootFlywayAutoConfiguration() {
        var classLoader = Thread.currentThread().getContextClassLoader();

        assertThat(classLoader.getResource(
                "org/springframework/boot/flyway/autoconfigure/FlywayAutoConfiguration.class"))
                .as("Spring Boot Flyway auto-configuration must be present so migrations run before JPA validation")
                .isNotNull();
    }
}
