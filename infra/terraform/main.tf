provider "contabo" {
  oauth2_client_id     = var.contabo_client_id
  oauth2_client_secret = var.contabo_client_secret
  oauth2_user          = var.contabo_user
  oauth2_pass          = var.contabo_password
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

resource "contabo_secret" "ssh" {
  name  = "quesale-ssh"
  type  = "ssh"
  value = var.ssh_public_key
}

resource "contabo_secret" "root" {
  name  = "quesale-root"
  type  = "password"
  value = var.root_password
}

resource "contabo_instance" "node" {
  count                = var.node_count
  existing_instance_id = length(var.existing_instance_ids) > count.index ? var.existing_instance_ids[count.index] : null
  display_name         = "quesale-node-${count.index + 1}"
  product_id           = var.product_id
  region               = var.region
  image_id             = var.image_id
  period               = var.period
  default_user         = "root"
  ssh_keys             = [tonumber(contabo_secret.ssh.id)]
  root_password        = tonumber(contabo_secret.root.id)
}

resource "cloudflare_dns_record" "app" {
  count   = var.node_count
  zone_id = var.cloudflare_zone_id
  name    = var.domain
  type    = "A"
  content = contabo_instance.node[count.index].ip_config[0].v4[0].ip
  ttl     = 300
  proxied = false
}

resource "cloudflare_dns_record" "turn" {
  zone_id = var.cloudflare_zone_id
  name    = "turn.${var.domain}"
  type    = "A"
  content = contabo_instance.node[0].ip_config[0].v4[0].ip
  ttl     = 300
  proxied = false
}
