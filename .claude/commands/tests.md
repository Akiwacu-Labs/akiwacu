---
description: Génère les tests unitaires d'une classe selon le pattern du projet
---

Écris les tests unitaires de `$ARGUMENTS`.

Contraintes :
- Pattern AAA (Arrange-Act-Assert), AssertJ, `@DisplayName` **en français**
- Pour **chaque** méthode publique : cas nominal + cas d'erreur métier + cas d'exception
- Services : Mockito pur, pas de contexte Spring
- Contrôleurs : `@WebMvcTest` + `MockMvc` + `@MockBean`
- Repositories : `@DataJpaTest`
- Si une règle R1–R8 est concernée, le test porte **exactement** le nom indiqué
  dans le tableau de CLAUDE.md et le `@DisplayName` commence par `Rn — `

Important : l'énoncé du TP exige que l'étudiant sache **expliquer chaque test à
la soutenance**. Donc :
- privilégie la lisibilité sur la concision ;
- pas de mock exotique, pas d'abstraction de test clever ;
- commente en français toute assertion non évidente ;
- termine par un résumé de 3 lignes en français expliquant ce que couvre la
  classe de test et quels bugs elle attraperait — je le collerai dans ma PR.
