package th.ac.dusit.dorm.platform;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SystemConfiguration {

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }
}
