# Infrastructure Akiwacu — codification des 3 VM existantes.
#
# ⚠ Ce code ne CRÉE pas les VM : elles ont été créées à la main au D1.
#    Il les DÉCRIT, et `terraform import` réconcilie l'état.
#    La preuve attendue au rapport est un `terraform plan` affichant "No changes".

terraform {
  required_version = ">= 1.6"
  required_providers {
    proxmox = {
      source  = "bpg/proxmox"
      version = "~> 0.66"
    }
  }
}

provider "proxmox" {
  endpoint  = var.proxmox_endpoint
  api_token = var.proxmox_api_token
  insecure  = true

  ssh {
    agent = true
  }
}

locals {
  vms = {
    "vm-dev-g1"    = { id = 301, cores = 2, memory = 2560, disk = 12, ip = "192.168.0.50" }
    "vm-prod-g1"   = { id = 302, cores = 2, memory = 2560, disk = 12, ip = "192.168.0.51" }
    "vm-devops-g1" = { id = 303, cores = 4, memory = 6144, disk = 25, ip = "192.168.0.52" }
  }
}

resource "proxmox_virtual_environment_vm" "akiwacu" {
  for_each = local.vms

  name      = each.key
  vm_id     = each.value.id
  node_name = var.proxmox_node
  tags      = ["akiwacu", "terraform"]

  # full = false → clone LIÉ. Les VM réelles sont des clones liés :
  # mettre true ferait diverger le plan et proposerait de les recréer.
  clone {
    vm_id = var.template_vm_id
    full  = false
  }

  cpu {
    cores = each.value.cores
    type  = "host"
  }

  memory {
    dedicated = each.value.memory
  }

  agent {
    enabled = true
  }

  disk {
    datastore_id = var.datastore_id
    interface    = "scsi0"
    size         = each.value.disk
  }

  network_device {
    bridge = "vmbr0"
    model  = "virtio"
  }

  initialization {
    ip_config {
      ipv4 {
        address = "${each.value.ip}/24"
        gateway = var.gateway
      }
    }

    user_account {
      username = "deployer"
      keys     = [var.ssh_public_key]
    }
  }

  serial_device {}

  # Ces champs sont fixés par cloud-init au clonage : les ignorer évite
  # un plan non vide à chaque exécution.
  lifecycle {
    ignore_changes = [
      initialization[0].user_account,
    ]
  }
}
