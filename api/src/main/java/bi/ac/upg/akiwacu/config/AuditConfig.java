package bi.ac.upg.akiwacu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * PROPRIÉTAIRE : Andy.
 * Alimente createdBy / updatedBy. Support de la règle R5 (traçabilité du validateur).
 *
 * @EnableJpaAuditing vit ici plutôt que sur AkiwacuApplication : posée sur la
 * classe @SpringBootApplication, elle s'active dans TOUT slice de test (même
 * @WebMvcTest, qui ne charge aucune entité) et casse le contexte avec "JPA
 * metamodel must not be empty". Ici, un @Configuration ordinaire, elle est
 * filtrée comme le reste par @WebMvcTest/@DataJpaTest.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfig {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a == null || !a.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(a.getName());
        };
    }
}
