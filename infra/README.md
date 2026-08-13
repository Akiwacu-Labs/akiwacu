# `infra/` — infrastructure codifiée

Propriétaire : **Klein**. Ces fichiers sont un livrable de l'énoncé.

```
infra/
├── ansible/                    ← configuration des 3 VM (utilisé dès le D1)
│   ├── ansible.cfg
│   ├── inventory/hosts.yml     ← 3 VM : app (.50 .51) + devops (.52)
│   ├── group_vars/all/         ← vault.yml chiffré, créé par Klein
│   └── playbooks/
│       ├── 00-base.yml         ← paquets, Docker, ufw, fuseau horaire — toutes
│       ├── 10-app.yml          ← dossier de déploiement, ports, node-exporter — app
│       ├── 20-monitoring.yml   ← Prometheus + Grafana — devops
│       └── 30-sonarqube.yml    ← SonarQube + sa base — devops
└── terraform/                  ← codification des VM (écrit au D7)
    ├── main.tf · variables.tf
    ├── terraform.tfvars.example
    └── import.sh               ← réconcilie l'état avec les VM existantes
```

## Ansible — l'ordre

> 🖥️ **SUR : ta machine**, dans `~/projects/Akiwacu/infra/ansible`

```bash
ansible all -m ping                              # 3 pong attendus
ansible-playbook playbooks/00-base.yml
ansible-playbook playbooks/10-app.yml
ansible-playbook playbooks/20-monitoring.yml --ask-vault-pass
ansible-playbook playbooks/30-sonarqube.yml  --ask-vault-pass
```

`ansible.cfg` déclare déjà l'inventaire : `-i inventory/hosts.yml` est inutile tant
que tu lances depuis ce dossier.

Les deux premiers playbooks n'ont pas besoin du vault. Les deux derniers oui —
crée-le d'abord, voir `group_vars/all/README.md`.

Collections requises, une fois :

```bash
ansible-galaxy collection install community.general community.docker ansible.posix
```

## Terraform — au D7, pas avant

Les VM existent déjà. Terraform ne sert pas à les créer mais à prouver que
l'infrastructure est décrite en code.

```bash
cd infra/terraform
cp terraform.tfvars.example terraform.tfvars   # puis remplir
terraform init
./import.sh                                    # importe les 3 VM puis affiche le plan
```

> **`terraform plan` affichant « No changes » est la capture d'écran attendue au
> rapport.** Si le plan propose de *détruire* ou *recréer* quoi que ce soit, l'import
> est incomplet : **n'applique pas**. Compare `clone.full`, `vm_id` et les tailles de
> disque à la réalité.
