package bi.ac.upg.akiwacu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * PROPRIÉTAIRE : Andy.
 * Alimente createdBy / updatedBy. Support de la règle R5 (traçabilité du validateur).
 */
@Configuration
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
