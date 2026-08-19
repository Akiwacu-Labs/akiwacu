package bi.ac.upg.akiwacu.auth;

import bi.ac.upg.akiwacu.utilisateur.Role;
import bi.ac.upg.akiwacu.utilisateur.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PROPRIÉTAIRE : Andy.
 * Émission et lecture des jetons JWT. Claims portées : sub (email),
 * utilisateurId, tontineId (base de R1, voir DECISIONS.md D-20), roles.
 * Signature HS256 : suffisant pour un monolithe qui émet et vérifie ses
 * propres jetons (pas de tiers à faire confiance).
 */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long expirationMinutes;

    public JwtService(@Value("${akiwacu.jwt.secret}") String secret,
                       @Value("${akiwacu.jwt.expiration-minutes}") long expirationMinutes) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String genererToken(Utilisateur utilisateur) {
        Instant maintenant = Instant.now();
        Instant expiration = maintenant.plus(expirationMinutes, ChronoUnit.MINUTES);

        List<String> roles = utilisateur.getRoles().stream()
                .map(Enum::name)
                .toList();

        return Jwts.builder()
                .subject(utilisateur.getEmail())
                .claim("utilisateurId", utilisateur.getId())
                .claim("tontineId", utilisateur.getTontine().getId())
                .claim("roles", roles)
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(expiration))
                .signWith(cle)
                .compact();
    }

    public Instant expirationDe(String token) {
        return extraireClaims(token).getExpiration().toInstant();
    }

    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    public Long extraireTontineId(String token) {
        return extraireClaims(token).get("tontineId", Long.class);
    }

    @SuppressWarnings("unchecked")
    public List<Role> extraireRoles(String token) {
        List<String> noms = extraireClaims(token).get("roles", List.class);
        return noms.stream().map(Role::valueOf).collect(Collectors.toList());
    }

    /**
     * Lève une sous-classe de io.jsonwebtoken.JwtException (signature invalide,
     * jeton expiré, jeton malformé) si le jeton n'est pas valide.
     * JwtAuthenticationFilter attrape cette exception : un jeton invalide ne
     * casse pas la requête, elle continue simplement non authentifiée.
     */
    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(cle)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
