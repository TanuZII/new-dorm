package th.ac.dusit.dorm.platform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration(proxyBeanMethods = false)
public class WebPaginationConfiguration {

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer maximumPageSize(DormProperties properties) {
        return resolver -> resolver.setMaxPageSize(properties.maxPageSize());
    }
}
