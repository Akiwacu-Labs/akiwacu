package bi.ac.upg.akiwacu.config;

import bi.ac.upg.akiwacu.auth.JwtAuthenticationFilter;
import bi.ac.upg.akiwacu.common.dto.ErreurResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * PROPRIÉTAIRE : Andy — personne d'autre ne modifie ce fichier.
 *
 * Stateless : chaque requête porte son propre JWT (JwtAuthenticationFilter),
 * aucune session côté serveur. /api/auth/login est la seule route métier
 * publique ; tout le reste exige un jeton valide, l'autorisation par rôle
 * étant affinée route par route dans chaque contrôleur (@PreAuthorize).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/actuator/health/**",
                    "/actuator/prometheus",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                // Ces deux cas surviennent au niveau du filtre, avant le DispatcherServlet :
                // GlobalExceptionHandler (@RestControllerAdvice) ne les voit jamais.
                // On écrit donc le même format ErreurResponse à la main, ici.
                .authenticationEntryPoint((requete, reponse, ex) ->
                    ecrireErreur(reponse, UNAUTHORIZED.value(), "Authentification requise", requete.getRequestURI()))
                .accessDeniedHandler((requete, reponse, ex) ->
                    ecrireErreur(reponse, FORBIDDEN.value(), "Rôle insuffisant pour cette opération", requete.getRequestURI()))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void ecrireErreur(jakarta.servlet.http.HttpServletResponse reponse, int statut, String message,
                               String chemin) throws IOException {
        reponse.setStatus(statut);
        reponse.setContentType(APPLICATION_JSON_VALUE);
        reponse.setCharacterEncoding("UTF-8");
        var corps = ErreurResponse.de(statut, statut == UNAUTHORIZED.value() ? "Unauthorized" : "Forbidden",
                message, chemin);
        reponse.getWriter().write(objectMapper.writeValueAsString(corps));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
