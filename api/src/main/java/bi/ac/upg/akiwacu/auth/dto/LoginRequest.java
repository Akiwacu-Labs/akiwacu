package bi.ac.upg.akiwacu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "l'email est obligatoire")
        @Email(message = "l'email doit être valide")
        String email,

        @NotBlank(message = "le mot de passe est obligatoire")
        String motDePasse) {
}
