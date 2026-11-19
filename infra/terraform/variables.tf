variable "contabo_client_id" {
  description = "Client ID de la API de Contabo"
  type        = string
  sensitive   = true
}

variable "contabo_client_secret" {
  description = "Client secret de la API de Contabo"
  type        = string
  sensitive   = true
}

variable "contabo_user" {
  description = "Correo de la cuenta de Contabo"
  type        = string
  sensitive   = true
}

variable "contabo_password" {
  description = "Contraseña de la API de Contabo"
  type        = string
  sensitive   = true
}

variable "ssh_public_key" {
  description = "Llave pública SSH que entra a las VPS"
  type        = string
}

variable "root_password" {
  description = "Contraseña de root de las VPS"
  type        = string
  sensitive   = true
}

variable "node_count" {
  description = "Cantidad de nodos del clúster"
  type        = number
  default     = 3
}

variable "product_id" {
  description = "Código del plan de Contabo"
  type        = string
  default     = "V153"
}

variable "region" {
  description = "Región de las VPS"
  type        = string
  default     = "US-central"
}

variable "image_id" {
  description = "ID de la imagen Ubuntu 24.04 de Contabo"
  type        = string
}

variable "period" {
  description = "Meses de contrato"
  type        = number
  default     = 1
}

variable "existing_instance_ids" {
  description = "IDs de las VPS ya compradas en el panel de Contabo, en orden"
  type        = list(string)
  default     = []
}

variable "domain" {
  description = "Dominio de la aplicación"
  type        = string
}

variable "cloudflare_api_token" {
  description = "Token de Cloudflare con permiso de editar DNS de la zona"
  type        = string
  sensitive   = true
}

variable "cloudflare_zone_id" {
  description = "Zone ID del dominio en Cloudflare"
  type        = string
}
