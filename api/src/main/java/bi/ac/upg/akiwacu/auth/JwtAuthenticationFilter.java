package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.common.TenantContext;
import bi.ac.upg.akiwacu.utilisateur.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * PROPRIÉTAIRE : Andy.
 * Un seul jeton lu par requête, jamais de session (voir SecurityConfig,
 * STATELESS). Pose l'Authentication ET le TenantContext (R1) à partir des
 * mêmes claims — les deux viennent du jeton, jamais du corps de la requête.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXE_BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest requete,
                                     @NonNull HttpServletResponse reponse,
                                     @NonNull FilterChain chaine) throws ServletException, IOException {
        try {
            String token = extraireToken(requete);
            if (token != null) {
                authentifier(token);
            }
            chaine.doFilter(requete, reponse);
        } catch (JwtException ex) {
            // Jeton invalide/expiré : on n'authentifie pas et on laisse la requête
            // continuer. Sur une route protégée, SecurityConfig renverra 401 ;
            // sur une route publique (login, swagger, actuator), rien ne change.
            chaine.doFilter(requete, reponse);
        } finally {
            // Tomcat réutilise ses threads : sans ce clear(), le tontineId de cet
            // utilisateur fuiterait vers la prochaine requête traitée par ce thread.
            TenantContext.clear();
        }
    }

    private String extraireToken(HttpServletRequest requete) {
        String entete = requete.getHeader("Authorization");
        if (entete == null || !entete.startsWith(PREFIXE_BEARER)) {
            return null;
        }
        return entete.substring(PREFIXE_BEARER.length());
    }

    private void authentifier(String token) {
        String email = jwtService.extraireEmail(token);
        Long tontineId = jwtService.extraireTontineId(token);
        List<Role> roles = jwtService.extraireRoles(token);

        List<GrantedAuthority> autorites = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .map(GrantedAuthority.class::cast)
                .toList();

        var authentication = new UsernamePasswordAuthenticationToken(email, null, autorites);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        TenantContext.setTontineId(tontineId);
    }
}
