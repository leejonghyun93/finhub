variable "namespace" {
  description = "배포 대상 네임스페이스"
  type        = string
}

variable "timeout" {
  description = "Helm 배포 타임아웃 (초)"
  type        = number
  default     = 1800
}

resource "helm_release" "finhub" {
  name      = "finhub"
  chart     = "${path.module}/../../helm/finhub"
  namespace = var.namespace

  timeout          = var.timeout
  create_namespace = false
  wait             = true

  lifecycle {
    ignore_changes = [metadata]
  }
}

output "helm_release_status" {
  value = helm_release.finhub.status
}
