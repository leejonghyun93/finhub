#!/bin/bash
# ============================================================
# FinHub Helm 배포 스크립트 (minikube)
# ============================================================
set -e

RELEASE_NAME="finhub"
CHART_PATH="./helm/finhub"
NAMESPACE="finhub"

echo "=== [1/4] minikube 상태 확인 ==="
minikube status || { echo "minikube가 실행되지 않았습니다. 'minikube start' 실행 후 재시도하세요."; exit 1; }

echo ""
echo "=== [2/4] Docker 이미지 minikube에 로드 ==="
# Docker Compose로 빌드된 이미지를 minikube에 로드
IMAGES=(
  "finhub-finhub-eureka:latest"
  "finhub-finhub-gateway:latest"
  "finhub-finhub-user:latest"
  "finhub-finhub-banking:latest"
  "finhub-finhub-investment:latest"
  "finhub-finhub-payment:latest"
  "finhub-finhub-insurance:latest"
  "finhub-finhub-search:latest"
  "finhub-finhub-notification:latest"
)

for IMAGE in "${IMAGES[@]}"; do
  echo "  Loading $IMAGE ..."
  minikube image load "$IMAGE"
done

echo ""
echo "=== [3/4] Namespace 생성 ==="
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

echo ""
echo "=== [4/4] Helm 배포 ==="
helm upgrade --install "$RELEASE_NAME" "$CHART_PATH" \
  --namespace "$NAMESPACE" \
  --create-namespace \
  --wait \
  --timeout 10m \
  --set jwtSecret="${JWT_SECRET:-finhub-secret-key-must-be-at-least-256-bits-long-for-hs256}"

echo ""
echo "=== 배포 완료! ==="
echo ""
echo "Gateway 접근 URL:"
minikube service "$RELEASE_NAME-gateway" --namespace "$NAMESPACE" --url

echo ""
echo "Pod 상태 확인:"
kubectl get pods -n "$NAMESPACE"
