output "namespace" {
  description = "배포된 Kubernetes 네임스페이스"
  value       = module.namespace.namespace
}

output "helm_release_status" {
  description = "Helm 릴리스 배포 상태"
  value       = module.finhub.helm_release_status
}
