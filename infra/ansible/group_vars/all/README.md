# Secrets Ansible

`vault.yml` est créé **par Klein**, chiffré, et se commite tel quel :

```bash
cd infra/ansible
ansible-vault create group_vars/all/vault.yml
```

Contenu :

```yaml
grafana_password:  <mot-de-passe-fort>
sonar_db_password: <mot-de-passe-fort>
```

**`vault.yml` n'est pas commité** — il est dans le `.gitignore`.

Un fichier chiffré par `ansible-vault` résiste techniquement à la publication, et
beaucoup d'équipes le versionnent. Ici le dépôt est **public** : la seule protection
serait la force de la passphrase, exposée au monde entier et hors ligne. Le rapport
coût/bénéfice ne tient pas pour huit jours de projet.

Conséquence : **Klein et Andy gardent chacun une copie locale** du vault. Si Klein
perd sa machine, Andy peut relancer les playbooks. C'est le seul point du projet où
un fichier utile ne vit pas dans Git — assume-le et mentionne-le au rapport, dans la
partie sécurité.

La passphrase ne s'écrit nulle part et se transmet de vive voix.

Les playbooks qui lisent ce fichier se lancent avec `--ask-vault-pass` :
`20-monitoring.yml` et `30-sonarqube.yml`. Les deux autres n'en ont pas besoin.
