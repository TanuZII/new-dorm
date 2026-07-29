package th.ac.dusit.dorm.platform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    OpenAPI dormitoryOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Dormitory Management API")
                .version("v1")
                .description("REST API for the university dormitory management system"));
    }
}
