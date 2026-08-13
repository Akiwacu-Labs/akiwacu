variable "proxmox_endpoint" {
  description = "URL de l'API Proxmox, ex. https://192.168.0.10:8006/"
  type        = string
}

variable "proxmox_api_token" {
  description = "Jeton terraform@pve!provider=..."
  type        = string
  sensitive   = true
}

variable "proxmox_node" {
  description = "Nom du nœud Proxmox"
  type        = string
  default     = "pve"
}

variable "template_vm_id" {
  description = "ID du template cloud-init dont les 3 VM sont des clones liés"
  type        = number
  default     = 9000
}

variable "datastore_id" {
  description = "Stockage des disques"
  type        = string
  default     = "local-lvm"
}

variable "gateway" {
  description = "Passerelle du LAN"
  type        = string
  default     = "192.168.0.1"
}

variable "ssh_public_key" {
  description = "Clé publique gha_deploy déposée dans cloud-init"
  type        = string
}
