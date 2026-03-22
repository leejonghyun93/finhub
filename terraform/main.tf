module "namespace" {
  source    = "./modules/namespace"
  namespace = var.namespace
}

module "finhub" {
  source    = "./modules/helm-release"
  namespace = module.namespace.namespace
  timeout   = var.helm_timeout

  depends_on = [module.namespace]
}
