---
description: Passe la checklist de revue de code du projet sur le diff courant
---

Relis le diff courant (`git diff develop...HEAD`) avec la checklist de revue du
projet. Pour chaque point : verdict + ligne concernée + correction suggérée.

- [ ] Règles métier dans le service, pas dans le contrôleur
- [ ] Cas d'erreur ET cas nominal testés
- [ ] Pas de secret, pas de valeur en dur
- [ ] Endpoint annoté OpenAPI (`@Operation`, `@ApiResponse`)
- [ ] `tontineId` pris depuis le JWT, jamais depuis le body (R1)
- [ ] Montants en `BigDecimal`, jamais `double`
- [ ] Injection par constructeur, pas `@Autowired` sur champ
- [ ] `@Transactional` sur le service, pas le contrôleur
- [ ] Exceptions métier typées, pas de `RuntimeException` nue
- [ ] Nommage cohérent avec le reste du projet
- [ ] Pas de logique dupliquée avec un domaine voisin
- [ ] Le diff fait moins de 400 lignes

Termine par : les 3 remarques les plus importantes, formulées comme des
commentaires de PR prêts à coller (précis, avec le numéro de ligne — pas de
"LGTM", pas de compliment vide).
