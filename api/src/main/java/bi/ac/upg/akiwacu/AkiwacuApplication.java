package bi.ac.upg.akiwacu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @EnableJpaAuditing vit dans config.AuditConfig, pas ici — voir sa javadoc.
@SpringBootApplication
public class AkiwacuApplication {

    public static void main(String[] args) {
        SpringApplication.run(AkiwacuApplication.class, args);
    }
}
