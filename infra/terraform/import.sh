#!/usr/bin/env bash
# Réconcilie l'état Terraform avec les 3 VM déjà créées à la main.
# À lancer UNE FOIS, depuis infra/terraform/, après `terraform init`.
set -euo pipefail

declare -A VMS=(
  ["vm-dev-g1"]=301
  ["vm-prod-g1"]=302
  ["vm-devops-g1"]=303
)

NODE="${PROXMOX_NODE:-pve}"

for NAME in "${!VMS[@]}"; do
  ID="${VMS[$NAME]}"
  echo "→ import $NAME (vmid $ID)"
  terraform import "proxmox_virtual_environment_vm.akiwacu[\"$NAME\"]" "$NODE/$ID"
done

echo
echo "Vérification — 'No changes' est le résultat attendu :"
terraform plan
