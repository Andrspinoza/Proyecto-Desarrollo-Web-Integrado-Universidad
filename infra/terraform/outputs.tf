output "node_ips" {
  description = "IP pública de cada nodo"
  value       = [for n in contabo_instance.node : n.ip_config[0].v4[0].ip]
}

output "app_url" {
  description = "URL pública de la API"
  value       = "https://${var.domain}"
}

output "deploy_host" {
  description = "IP del nodo 1 para el secreto DEPLOY_HOST del CD"
  value       = contabo_instance.node[0].ip_config[0].v4[0].ip
}

output "ansible_inventory" {
  description = "Contenido de infra/ansible/inventory.ini"
  value = join("\n", concat(
    ["[k3s_servers]"],
    [for i, n in contabo_instance.node : "node${i + 1} ansible_host=${n.ip_config[0].v4[0].ip} wg_ip=10.10.0.${i + 1} ansible_user=root"],
    [""],
    ["[k3s_servers:vars]", "ansible_ssh_private_key_file=~/.ssh/id_ed25519"],
  ))
}
