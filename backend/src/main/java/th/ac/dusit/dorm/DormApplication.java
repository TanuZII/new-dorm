package th.ac.dusit.dorm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DormApplication {
    public static void main(String[] args) {
        SpringApplication.run(DormApplication.class, args);
    }
}
