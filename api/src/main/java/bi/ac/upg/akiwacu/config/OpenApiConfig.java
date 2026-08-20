package bi.ac.upg.akiwacu.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PROPRIÉTAIRE : Andy.
 * Déclare le schéma de sécurité "bearerAuth" utilisé par Swagger UI : le
 * bouton Authorize demande un JWT (obtenu via POST /api/auth/login) et
 * l'ajoute automatiquement à chaque requête d'essai.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_JWT = "bearerAuth";

    @Bean
    OpenAPI akiwacuOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Akiwacu — API de gestion des tontines")
                        .description("API REST pour la gestion d'associations d'épargne et de "
                                + "crédit communautaires. Projet académique, UPG Gitega.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_JWT))
                .components(new Components()
                        .addSecuritySchemes(SCHEME_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
