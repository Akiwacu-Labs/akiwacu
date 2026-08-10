# ADR-002 — Runner GitHub Actions auto-hébergé

**Statut :** acceptée · **Date :** D1 · **Auteur :** @GKcoding-prog · **Décision liée :** `DECISIONS.md` D-13

---

## Contexte

L'énoncé impose : « Les déploiements devront être réalisés automatiquement par
GitHub Actions **via SSH**. »

Les runners hébergés par GitHub tournent dans le cloud. Notre serveur Proxmox est sur
le LAN de l'université, derrière du NAT, sans adresse IP publique.

**Une connexion SSH sortante depuis le cloud vers `192.168.0.50` est impossible.**
Ce n'est pas un problème de configuration : l'adresse n'est pas routable depuis
l'extérieur. Aucun réglage de workflow ne contourne cela.

C'est le point où la plupart des groupes se bloqueront, et généralement tard — au
moment de brancher le déploiement, quand il ne reste plus de marge.

## Options envisagées

| Option | Verdict |
|---|---|
| **Runner auto-hébergé sur `vm-devops-g1`** | Retenue |
| Cloudflare Tunnel ou Tailscale pour exposer SSH | Fonctionne, mais ajoute une dépendance externe à expliquer et à maintenir |
| IP publique + redirection de port | Nécessite l'accord de l'administrateur réseau, et ouvre une surface d'attaque |
| SonarCloud à la place de SonarQube | Contourne une partie du problème, mais l'énoncé dit « SonarQube » |

## Décision

**Un runner GitHub Actions auto-hébergé, installé sur `vm-devops-g1` (192.168.0.52),
sous un utilisateur dédié — jamais root.**

Le runner établit une connexion **sortante** en HTTPS vers GitHub et attend les jobs.
Aucune règle de pare-feu entrante, aucune IP publique, aucune demande à
l'administrateur réseau. Une fois le job démarré, il s'exécute **à l'intérieur du
LAN** : il joint SonarQube en local et fait du SSH vers les VM sur le réseau interne.

```
GitHub.com  ──(HTTPS sortant, initié par le runner)──►  vm-devops-g1
                                                        runner + SonarQube
                                                        + Prometheus + Grafana
                                                             │  LAN
                                                             ├──SSH──► vm-dev-g1   .50
                                                             ├──SSH──► vm-prod-g1  .51
                                                             └──push─► GHCR (sortant)
```

Dans les workflows : `runs-on: [self-hosted, linux, x64, onprem]`.

## Conséquences

**C'est la dépendance la plus critique du projet.** Si le runner tombe, plus rien ne
se construit et plus rien ne se déploie. Il est donc installé au **D1**, avant
d'écrire le moindre job de déploiement, et son état `Idle` est vérifié dans
`Settings → Actions → Runners`.

**Le jeton d'enregistrement expire en une heure.** Andy le génère au moment où Klein
est prêt à taper la commande, pas la veille.

**Le runner n'est pas éphémère.** Contrairement à un runner GitHub, il conserve son
espace de travail entre les jobs. Deux conséquences :

- prévoir un nettoyage des images Docker, sinon le disque de `vm-devops-g1` se
  remplit en quelques jours ;
- ne jamais écrire de secret en clair dans le workspace : il survit au job.

**Le dépôt est public.** Un runner auto-hébergé sur un dépôt public accepterait, sans
précaution, d'exécuter le code d'une PR venue de l'extérieur sur notre machine. Le
déclenchement est donc limité aux branches du dépôt, et l'approbation manuelle des
workflows pour les contributeurs externes reste activée.

## Preuve à montrer dans le rapport

- Capture de `Settings → Actions → Runners` avec le runner en **Idle**
- Le schéma de flux ci-dessus
- Un extrait de log de déploiement montrant le SSH vers `192.168.0.51`

## Question probable à la soutenance

*« Pourquoi ne pas avoir utilisé les runners GitHub, qui sont gratuits et
maintenus ? »*

Répondre par la topologie réseau, pas par une préférence : les VM n'ont pas d'adresse
routable depuis Internet. Puis présenter les trois alternatives du tableau et dire
pourquoi chacune a été écartée. C'est cette comparaison qui fait la valeur de la
réponse, pas la décision elle-même.
