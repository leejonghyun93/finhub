variable "namespace" {
  description = "생성할 Kubernetes 네임스페이스"
  type        = string
}

resource "kubernetes_namespace" "finhub" {
  metadata {
    name = var.namespace
    labels = {
      app     = "finhub"
      managed = "terraform"
    }
  }
}

output "namespace" {
  value = kubernetes_namespace.finhub.metadata[0].name
}
