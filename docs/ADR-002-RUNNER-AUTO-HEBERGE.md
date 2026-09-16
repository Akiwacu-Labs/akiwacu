# ADR-002 — Runner GitHub Actions auto-hébergé

**Statut :** acceptée · **Date :** D1 · **Auteur :** Klein · **Décision liée :** `DECISIONS.md` D-13

Ce document est destiné au **chapitre 4 du rapport technique**. C'est la décision
d'architecture la plus structurante du projet.

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

**Le réglage TCP de la VM était le vrai goulot — pas la bande passante.**

Symptôme : `Prepare all required actions` échouait sur
`HttpClient.Timeout of 100 seconds elapsing`, trois tentatives, puis abandon.
Le runner télécharge **toutes** les actions avant d'exécuter la moindre étape :
une seule action injoignable fait échouer le job entier, et `continue-on-error`
n'y peut rien puisque l'échec précède les étapes.

La tentation était de supprimer les actions tierces. Mesurer d'abord a évité
une fausse solution :

| Mesure depuis `vm-devops-g1` | Résultat |
|---|---|
| `codeload.github.com`, 1 connexion | **16 Ko/s** — 2,2 Mo en 135 s, au-delà du délai de 100 s |
| `codeload.github.com`, 4 connexions | **188 Ko/s cumulés** (74 + 46 + 45 + 22) |
| `speed.cloudflare.com`, 1 connexion | **562 Ko/s** |
| IPv6 · MTU 1500 · DNS · perte de paquets | aucun problème · RTT 500 ms vers Francfort |

Diagnostic : la liaison délivre 4,5 Mbit/s. Une **connexion unique** vers
GitHub plafonnait à 16 Ko/s, soit une fenêtre TCP d'environ 8 Ko sur un RTT de
0,5 s — le débit d'un flux vaut fenêtre ÷ latence. Sur un chemin à forte
latence, le contrôle de congestion par défaut ne fait jamais croître la fenêtre.

Correctif — `/etc/sysctl.d/99-akiwacu-net.conf` :

```
net.core.default_qdisc          = fq
net.ipv4.tcp_congestion_control = bbr
net.core.rmem_max               = 16777216
net.core.wmem_max               = 16777216
net.ipv4.tcp_rmem               = 4096 87380 16777216
net.ipv4.tcp_wmem               = 4096 65536 16777216
```

**Résultat : 16 Ko/s → 49 Ko/s**, le même fichier passe de 135 s à 45 s, sous
le délai. Les actions standard sont conservées.

Deux ajustements demeurent, justifiés par la mesure et non par le symptôme :

- **L'outillage est pré-installé sur la VM** (JDK 21, Node 20, jq).
  `setup-java` téléchargerait 180 Mo à chaque exécution — une heure à 49 Ko/s.
  Un runner GitHub part d'une image dont le cache d'outils est chaud ; le nôtre
  est une machine persistante, donc on installe une fois.
- **Trivy tourne en conteneur**, base de vulnérabilités en cache sur disque.
  `trivy-action` retélécharge son binaire (~50 Mo) à chaque exécution.

Ce qui reste téléchargé l'est **une seule fois** : `~/.m2` et
`~/actions-runner/_work/_actions/` persistent entre les jobs.

**La leçon, et c'est elle qui vaut d'être dite à la soutenance :** un délai
dépassé ressemble à un problème de débit. Ici, le débit était bon et le
paramétrage TCP mauvais. Supprimer les actions aurait « réparé » le pipeline
en masquant la cause, et le même plafond aurait frappé Maven, Docker et
SonarQube ensuite.

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
