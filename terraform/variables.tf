variable "namespace" {
  description = "Kubernetes 네임스페이스"
  type        = string
  default     = "finhub"
}

variable "helm_timeout" {
  description = "Helm 배포 타임아웃 (초)"
  type        = number
  default     = 1800
}

variable "kubeconfig_path" {
  description = "kubeconfig 파일 경로"
  type        = string
  default     = "~/.kube/config"
}
