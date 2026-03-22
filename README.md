# FinHub 💰

<div align="center">

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=for-the-badge&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.111-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=for-the-badge&logo=typescript&logoColor=white)

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.6.0-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.13-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Helm-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Terraform](https://img.shields.io/badge/Terraform-1.6+-7B42BC?style=for-the-badge&logo=terraform&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

**MSA 기반 개인 통합 금융 플랫폼**

토스(Toss) 스타일의 UI로 뱅킹, 투자, 결제, 보험을 하나의 앱에서 관리하는<br/>
**마이크로서비스 아키텍처 금융 플랫폼** + **LangGraph AI 금융 어시스턴트**

</div>

---

## 📌 프로젝트 소개

**FinHub**는 Spring Cloud 기반의 MSA 아키텍처로 구성된 개인 통합 금융 플랫폼입니다.
9개의 Java Spring Boot 마이크로서비스와 1개의 Python FastAPI AI 서비스가
Eureka 서비스 디스커버리와 API Gateway를 통해 유기적으로 연결됩니다.

**LangGraph 기반 AI 에이전트**가 사용자의 금융 질문 의도를 분류하고,
5개의 전문 에이전트(계좌/소비/상품/투자/일반)와 pgvector RAG로 맞춤 답변을 제공합니다.

### 🎯 핵심 특징

| 특징 | 설명 |
|------|------|
| 🏗️ **MSA 아키텍처** | 10개 독립 서비스 — 독립 배포 및 수평 확장 가능 |
| 🤖 **AI 어시스턴트** | LangGraph 의도 분류 → 5개 전문 에이전트 → pgvector RAG |
| ⚡ **이벤트 드리븐** | Kafka 비동기 이벤트로 서비스 간 느슨한 결합 |
| 🔐 **JWT 인증** | AccessToken(30분) + RefreshToken(7일) + Redis 세션 |
| ☸️ **Kubernetes 배포** | Helm Chart로 minikube 원클릭 배포 |
| 📊 **모니터링** | Prometheus + Grafana 실시간 메트릭 대시보드 |
| 🔄 **CI/CD** | GitHub Actions — PR 빌드/테스트 + 자동 Docker 이미지 배포 |
| ✅ **단위 테스트** | JUnit5 + Mockito + pytest — 40케이스 (Java 22 + Python 18) |
| 🔁 **Saga 패턴** | Choreography-based Saga로 분산 트랜잭션 보장 (송금 실패 시 보상 이벤트) |

---

## ✨ 주요 기능

<details>
<summary><b>🏦 Banking — 뱅킹</b></summary>

- 입출금 계좌 개설 및 관리
- 실시간 계좌 잔액 조회
- 계좌 간 송금 처리
- 잔액 충전
- 거래내역 페이징 조회

</details>

<details>
<summary><b>📈 Investment — 투자</b></summary>

- 주식 종목 목록 조회 및 실시간 가격
- 포트폴리오 생성 및 관리
- 주식 매수 / 매도 실행
- 보유 종목 수익률 계산 (평가금액, 손익, 수익률)
- 매매 내역 조회

</details>

<details>
<summary><b>💳 Payment — 결제</b></summary>

- 카드 등록 및 결제 수단 관리
- 결제 처리 및 내역 조회

</details>

<details>
<summary><b>🛡️ Insurance — 보험</b></summary>

- 보험 상품 목록 및 상세 조회
- 보험 가입 / 해지 및 가입 내역 관리

</details>

<details>
<summary><b>🔍 Search — 검색</b></summary>

- Elasticsearch 기반 금융 상품 통합 검색
- 카테고리별 필터링 (주식, 보험, 계좌)

</details>

<details>
<summary><b>🔔 Notification — 알림</b></summary>

- Kafka 이벤트 기반 실시간 알림 수신
- 알림 읽음 처리 / 전체 읽음 / 삭제

</details>

<details>
<summary><b>🤖 AI Assistant — AI 어시스턴트</b></summary>

- LangGraph 의도 분류 라우터 (5개 에이전트)
- 계좌 잔액/거래 조회, 소비 패턴 분석
- 금융 상품 추천 (pgvector RAG)
- 대화 히스토리 유지 (세션 기반)

</details>

---

## 🛠️ 기술 스택

### Backend (Java)
| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Cloud | Spring Cloud 2023.0.2 (Eureka, Gateway MVC) |
| Build | Maven Multi-Module |
| Security | Spring Security + JWT (JJWT) |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway |
| Metrics | Micrometer + Prometheus |
| Testing | JUnit5 + Mockito (단위 테스트 22케이스) |

### AI Service (Python)
| 분류 | 기술 |
|------|------|
| Language | Python 3.11 |
| Framework | FastAPI 0.111 |
| AI Orchestration | LangGraph (StateGraph) |
| LLM | Ollama (llama3.2) |
| Embeddings | nomic-embed-text |
| Vector DB | pgvector (PostgreSQL 확장) |
| Auth | JWT 검증 (python-jose) |
| Testing | pytest + pytest-asyncio (단위 테스트 18케이스) |

### Frontend
| 분류 | 기술 |
|------|------|
| Framework | React 18 + TypeScript 5.2 |
| Build | Vite 5.3 |
| Styling | Tailwind CSS 3.4 |
| State | Zustand 4.5 |
| Routing | React Router v6 |
| HTTP | Axios 1.7 |

### Infrastructure & DevOps
| 분류 | 기술 | 용도 |
|------|------|------|
| Database | PostgreSQL 16 + pgvector | 서비스별 독립 DB + 벡터 검색 |
| Cache | Redis 7 | JWT 세션, 토큰 블랙리스트 |
| Message Queue | Apache Kafka 7.6.0 | 서비스 간 이벤트 통신 |
| Search | Elasticsearch 8.13 | 풀텍스트 검색 |
| LLM Runtime | Ollama | 로컬 LLM 추론 |
| Container | Docker + Docker Compose | 로컬 개발 환경 |
| Orchestration | Kubernetes + Helm | 프로덕션 배포 |
| IaC | Terraform 1.6+ | K8s 리소스 프로비저닝 자동화 |
| Monitoring | Prometheus + Grafana | 메트릭 수집 및 대시보드 |
| CI/CD | GitHub Actions | 자동 빌드/테스트/배포 |

---

## 🏗️ 시스템 아키텍처

```
┌──────────────────────────────────────────────────────────────────┐
│                     Client (Browser)                             │
│              React 18 + TypeScript  :3000                        │
└─────────────────────────┬────────────────────────────────────────┘
                          │  HTTP /api/v1/**
┌─────────────────────────▼────────────────────────────────────────┐
│                      API Gateway  :8080                           │
│          Spring Cloud Gateway MVC — JWT 검증 · 라우팅             │
└──┬───────┬───────┬───────┬───────┬───────┬──────┬───────┬───────┘
   │       │       │       │       │       │      │       │
   ▼       ▼       ▼       ▼       ▼       ▼      ▼       ▼
:8081   :8082   :8083   :8084   :8085   :8086  :8087   :8088
 user  banking invest  payment insur  search   ai    notif
   │       │       │       │       │             │
   └───────┴───────┴───────┴───────┘             │
                   │  Kafka Events                │
                   └──────────────────────────────┘
                                                  ▲
                    ┌──────────────┐              │
                    │ Eureka :8761 │◄─── 서비스 등록/디스커버리
                    └──────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                       Infrastructure                             │
│  PostgreSQL :5432  ·  Redis :6379  ·  Kafka :9092               │
│  Elasticsearch :9200  ·  Ollama :11434                           │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        Monitoring                                │
│           Prometheus :9090  ·  Grafana :3000                     │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🔧 MSA 서비스 구성

### 애플리케이션 서비스 (10개)

| 서비스 | 포트 | 언어 | 설명 | DB |
|--------|------|------|------|----|
| `finhub-eureka` | 8761 | Java | Eureka Service Registry | — |
| `finhub-gateway` | 8080 | Java | API Gateway · JWT 검증 · 라우팅 | — |
| `finhub-user` | 8081 | Java | 사용자 관리 · JWT 인증 발급 | finhub_user |
| `finhub-banking` | 8082 | Java | 계좌 · 송금 · 거래 관리 | finhub_banking |
| `finhub-investment` | 8083 | Java | 투자 포트폴리오 · 매매 | finhub_investment |
| `finhub-payment` | 8084 | Java | 결제 수단 · 결제 처리 | finhub_payment |
| `finhub-insurance` | 8085 | Java | 보험 상품 · 가입 관리 | finhub_insurance |
| `finhub-search` | 8086 | Java | Elasticsearch 통합 검색 | — |
| `finhub-ai` | 8087 | Python | LangGraph AI 어시스턴트 | finhub_ai |
| `finhub-notification` | 8088 | Java | Kafka 알림 소비자 | finhub_notification |

### 인프라 서비스 (8개)

| 서비스 | 포트 | 용도 |
|--------|------|------|
| PostgreSQL | 5432 | 서비스별 독립 DB + pgvector |
| Redis | 6379 | JWT 세션 · 토큰 블랙리스트 |
| Zookeeper | 2181 | Kafka 코디네이터 |
| Kafka | 9092 | 이벤트 메시지 브로커 |
| Elasticsearch | 9200 | 풀텍스트 검색 엔진 |
| Ollama | 11434 | 로컬 LLM 추론 서버 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 모니터링 대시보드 |

> **기동 순서**: `eureka` → `gateway` → 도메인 서비스

---

## 🤖 AI 에이전트 구조

```
사용자 메시지
     │
     ▼
┌─────────────────────────────────────────────────────────┐
│             intent_classifier_node                       │
│      LLM (temperature=0) — 의도 5가지로 분류             │
└───┬─────────┬──────────┬───────────┬────────────────────┘
    │         │          │           │
    ▼         ▼          ▼           ▼
ACCOUNT_  SPENDING_  PRODUCT_   GENERAL
INQUIRY   ANALYSIS   SEARCH
    │         │          │           │
    ▼         ▼          ▼           ▼
account_  analysis_  product_   general_
 agent     agent      agent      agent
    │         │          │           │
    │         │    ┌──────────────────▼──┐
    │         │    │   pgvector RAG      │
    │         │    │  nomic-embed-text   │
    │         │    │  유사도 검색 Top-K   │
    │         │    └─────────────────────┘
    │         │          │
    └─────────┴──────────┘
                 │
           Tool Calling
    ┌────────────┼─────────────┐
    ▼            ▼             ▼
get_account_ get_spending_ search_
  balance     analysis    products
    │            │             │
    └────────────┴─────────────┘
                 │
           최종 응답 생성
     { response, intent, tools_used }
```

### 의도 분류 (5가지)

| 의도 | 에이전트 | 설명 |
|------|---------|------|
| `ACCOUNT_INQUIRY` | account_agent | 계좌 잔액 · 거래 내역 조회 |
| `SPENDING_ANALYSIS` | analysis_agent | 소비 패턴 분석 |
| `PRODUCT_SEARCH` | product_agent | 금융 상품 추천 (RAG) |
| `INSURANCE` | product_agent | 보험 상품 추천 (RAG) |
| `GENERAL` | general_agent | 금융 일반 상담 |

---

## 📨 Kafka 이벤트 플로우

```
finhub-user        ──[user.registered]──────────────────────► finhub-notification
finhub-banking     ──[banking.transfer.completed]────────────► finhub-notification
finhub-payment     ──[payment.completed]─────────────────────► finhub-notification
finhub-investment  ──[investment.trade.completed]────────────► finhub-notification
finhub-insurance   ──[insurance.subscribed]──────────────────► finhub-notification
```

| 토픽 | 발행 서비스 | 내용 |
|------|-----------|------|
| `user.registered` | finhub-user | 신규 회원가입 |
| `banking.transfer.completed` | finhub-banking | 송금 완료 (출금/입금 계좌, 금액) |
| `payment.completed` | finhub-payment | 결제 완료 (가맹점, 금액) |
| `investment.trade.completed` | finhub-investment | 매매 완료 (종목, 수량, 금액) |
| `insurance.subscribed` | finhub-insurance | 보험 가입 완료 |

**Consumer Group**: `notification-group`

---

## ☸️ Kubernetes 배포 구조 (Helm)

```
helm/finhub/
├── Chart.yaml
├── values.yaml                 # 전체 설정 관리
└── templates/
    ├── configmap.yaml          # 공통 환경변수
    ├── secret.yaml             # JWT Secret, DB 패스워드
    ├── initdb-job.yaml         # DB 초기화 Job
    │
    ├── postgres.yaml           # StatefulSet + PVC (2Gi)
    ├── redis.yaml
    ├── zookeeper.yaml
    ├── kafka.yaml
    ├── elasticsearch.yaml      # StatefulSet + PVC (2Gi)
    ├── ollama.yaml             # PVC (10Gi) — optional
    │
    ├── eureka.yaml
    ├── gateway.yaml            # NodePort :30080
    ├── user.yaml
    ├── banking.yaml
    ├── investment.yaml
    ├── payment.yaml
    ├── insurance.yaml
    ├── search.yaml
    ├── ai.yaml
    ├── notification.yaml
    │
    └── monitoring.yaml         # Prometheus + Grafana (NodePort :30030)
```

### NodePort 접근 (minikube)

| 서비스 | NodePort |
|--------|----------|
| API Gateway | :30080 |
| Grafana | :30030 |

---

## 📊 모니터링 (Prometheus + Grafana)

모든 Spring Boot 서비스에 **Micrometer + Prometheus** 메트릭이 적용되어 있습니다.

```yaml
# 각 서비스 application.yml
management:
  endpoints.web.exposure.include: health,prometheus,info
  endpoint.prometheus.enabled: true
```

### Grafana 대시보드 패널 (6개)

| 패널 | 메트릭 |
|------|--------|
| 📈 RPS (초당 요청수) | `rate(http_server_requests_seconds_count[1m])` |
| ⏱️ 평균 응답시간 | `rate(http_server_requests_seconds_sum[1m])` |
| 💾 JVM Heap 메모리 | `jvm_memory_used_bytes{area="heap"}` |
| 🧵 JVM 스레드 수 | `jvm_threads_live_threads` |
| ❌ 에러율 | `rate(http_server_requests_seconds_count{status=~"5.."}[1m])` |
| 🖥️ CPU 사용률 | `process_cpu_usage` |

### Prometheus 스크레이프 대상 (8개 서비스)

`finhub-user`, `finhub-banking`, `finhub-investment`, `finhub-payment`,
`finhub-insurance`, `finhub-search`, `finhub-notification`, `finhub-gateway`

---

## 🔄 CI/CD (GitHub Actions)

```
.github/workflows/
├── ci.yml    # PR → main, develop 브랜치
└── cd.yml    # push → main 브랜치
```

### ci.yml — 자동 빌드 & 테스트

```
PR 생성 (main / develop)
    │
    ├── build-spring (matrix × 9)
    │     └── Maven clean install + test (Java 17)
    │
    ├── lint-python
    │     └── flake8 + black format check (Python 3.11)
    │
    └── ci-success
          └── 전체 결과 집계
```

### cd.yml — 자동 Docker 이미지 빌드 & 푸시

```
push → main
    │
    ├── docker-spring (matrix × 9)
    │     └── Maven 빌드 → Docker Hub push
    │           {DOCKER_USERNAME}/finhub-{service}:latest
    │           {DOCKER_USERNAME}/finhub-{service}:sha-xxxxxxx
    │
    ├── docker-ai
    │     └── FastAPI Docker Hub push
    │
    └── [Optional] deploy-k8s
          └── Helm upgrade → EKS/GKE (주석 처리 — 실 클러스터 연결 시 활성화)
```

**필요한 GitHub Secrets**: `DOCKER_USERNAME`, `DOCKER_PASSWORD`

---

## 🚀 실행 방법

### ✅ Prerequisites

- Docker Desktop
- Java 17+ (로컬 개발 시)
- Node.js 18+ (프론트엔드 개발 시)
- minikube + kubectl + Helm (Kubernetes 배포 시)

---

### 🐳 Docker Compose로 실행 (권장)

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/finhub.git
cd finhub

# 2. 전체 서비스 실행 (인프라 + 앱)
docker-compose up -d

# 3. 서비스 상태 확인
docker-compose ps

# 4. 로그 확인
docker-compose logs -f finhub-user
```

**접속 URL**

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |

---

### ☸️ Kubernetes (minikube)로 실행

```bash
# 1. minikube 시작
minikube start --memory=8192 --cpus=4

# 2. minikube Docker 환경 연결
eval $(minikube docker-env)

# 3. Docker 이미지 빌드 (minikube 내부로 직접 빌드)
docker-compose build

# 4. finhub 네임스페이스 생성
kubectl create namespace finhub

# 5. Helm 배포
helm install finhub helm/finhub -n finhub --timeout 30m

# 6. 배포 상태 확인
kubectl get pods -n finhub

# 7. API Gateway 접근
minikube service finhub-gateway -n finhub
# 또는 NodePort: http://$(minikube ip):30080

# 8. Grafana 모니터링 접근
# NodePort: http://$(minikube ip):30030
# 기본 계정: admin / admin
```

**Helm 업그레이드**

```bash
# 설정 변경 후 업그레이드
kubectl delete job finhub-initdb -n finhub --ignore-not-found=true
helm upgrade finhub helm/finhub -n finhub --timeout 30m
```

---

### 🏗️ Terraform으로 배포

> Kubernetes Provider + Helm Provider로 minikube 배포를 자동화합니다.

#### Prerequisites

- [Terraform 1.6+](https://developer.hashicorp.com/terraform/install)
- minikube 실행 중 + kubeconfig 설정 완료

```bash
# terraform/ 디렉토리로 이동
cd terraform

# 1. 프로바이더 초기화
terraform init

# 2. 배포 계획 확인
terraform plan

# 3. 배포 실행 (네임스페이스 생성 + Helm 릴리스)
terraform apply

# 4. 배포 상태 확인
terraform output
```

**변수 오버라이드**

```bash
# 네임스페이스나 타임아웃 변경
terraform apply \
  -var="namespace=finhub-prod" \
  -var="helm_timeout=900"
```

**리소스 제거**

```bash
terraform destroy
```

---

### 🛠️ 로컬 개발 환경 실행

```bash
# 인프라만 실행
docker-compose up -d postgres redis zookeeper kafka elasticsearch

# 백엔드 전체 빌드
./mvnw clean install -DskipTests

# 기동 순서에 따라 실행 (각각 별도 터미널)
./mvnw spring-boot:run -pl finhub-eureka
./mvnw spring-boot:run -pl finhub-gateway
./mvnw spring-boot:run -pl finhub-user
./mvnw spring-boot:run -pl finhub-banking
./mvnw spring-boot:run -pl finhub-investment
./mvnw spring-boot:run -pl finhub-payment
./mvnw spring-boot:run -pl finhub-insurance
./mvnw spring-boot:run -pl finhub-search
./mvnw spring-boot:run -pl finhub-notification

# AI 서비스 (별도 터미널)
cd finhub-ai
pip install -r requirements.txt
uvicorn main:app --reload --port 8087

# 프론트엔드
cd finhub-frontend
npm install
npm run dev  # http://localhost:3000
```

---

## 📡 주요 API 엔드포인트

> 🔒 마크가 있는 API는 `Authorization: Bearer {accessToken}` 헤더 필요

### 🔐 User — 인증

```
POST   /api/v1/users/signup          회원가입
POST   /api/v1/users/login           로그인 → { accessToken, refreshToken }
POST   /api/v1/users/logout          로그아웃 🔒
POST   /api/v1/users/reissue         토큰 재발급
GET    /api/v1/users/me              내 정보 조회 🔒
```

### 🏦 Banking — 계좌 · 거래

```
POST   /api/v1/banking/accounts                          계좌 개설 🔒
GET    /api/v1/banking/accounts                          계좌 목록 🔒
POST   /api/v1/banking/accounts/{id}/deposit             잔액 충전 🔒
POST   /api/v1/banking/accounts/transfer                 송금 🔒
GET    /api/v1/banking/accounts/{id}/transactions        거래내역 🔒 (페이징)
```

### 📈 Investment — 투자

```
GET    /api/v1/investment/stocks                              종목 목록
POST   /api/v1/investment/portfolios                          포트폴리오 생성 🔒
GET    /api/v1/investment/portfolios/{id}/holdings            보유 종목 🔒
POST   /api/v1/investment/trade                               매매 실행 🔒
GET    /api/v1/investment/trade/history                       매매 내역 🔒
```

### 💳 Payment — 결제

```
POST   /api/v1/payment/methods       결제 수단 등록 🔒
POST   /api/v1/payment/pay           결제 처리 🔒
GET    /api/v1/payment/history       결제 내역 🔒
```

### 🛡️ Insurance — 보험

```
GET    /api/v1/insurance/products             보험 상품 목록
POST   /api/v1/insurance/subscribe            보험 가입 🔒
GET    /api/v1/insurance/subscriptions        가입 내역 🔒
DELETE /api/v1/insurance/subscriptions/{id}  보험 해지 🔒
```

### 🔍 Search — 검색

```
GET    /api/v1/search?keyword=&category=&page=0&size=10   통합 검색
```

### 🔔 Notification — 알림

```
GET    /api/v1/notification                        알림 목록 🔒
PATCH  /api/v1/notification/{id}/read              읽음 처리 🔒
PATCH  /api/v1/notification/read-all               전체 읽음 🔒
DELETE /api/v1/notification/{id}                   알림 삭제 🔒
```

### 🤖 AI — AI 어시스턴트

```
POST   /api/v1/ai/chat               AI 채팅 🔒
         → { reply, intent, tools_used, session_id }
GET    /api/v1/ai/recommend          금융 상품 추천 (RAG)
GET    /api/v1/ai/health             헬스체크
```

---

## 🔑 인증 플로우

```
1. POST /api/v1/users/login
         ↓
2. finhub-user → JWT 발급
   AccessToken (30분) + RefreshToken (7일)
         ↓
3. Redis에 RefreshToken 저장
         ↓
4. 클라이언트 → localStorage에 AccessToken 저장
         ↓
5. 이후 모든 요청: Authorization: Bearer {accessToken}
         ↓
6. finhub-gateway → JWT 검증 → 서비스 라우팅
```

---

## 📁 프로젝트 구조

```
finhub/
├── 📄 pom.xml                        # Root POM (Spring Boot 3.3.0 Multi-Module)
├── 🐳 docker-compose.yml             # 전체 인프라 + 서비스 구성
├── ⚙️  .github/workflows/
│   ├── ci.yml                        # PR 빌드/테스트
│   └── cd.yml                        # Docker Hub 배포
│
├── ☸️  helm/finhub/                  # Helm Chart
│   ├── values.yaml                   # 전체 배포 설정
│   └── templates/                    # 20개 K8s 리소스 템플릿
│
├── 🏗️  terraform/                    # Terraform IaC
│   ├── providers.tf                  # Kubernetes + Helm 프로바이더
│   ├── variables.tf                  # namespace, timeout, kubeconfig_path
│   ├── main.tf                       # 네임스페이스 + Helm 릴리스 모듈 호출
│   ├── outputs.tf                    # namespace, helm_release_status
│   └── modules/
│       ├── namespace/main.tf         # kubernetes_namespace 리소스
│       └── helm-release/main.tf      # helm_release 리소스
│
├── 🔭 finhub-eureka/                 # Service Registry
├── 🚪 finhub-gateway/                # API Gateway + JWT 필터
├── 👤 finhub-user/                   # 사용자 · 인증 서비스
├── 🏦 finhub-banking/                # 계좌 · 거래 서비스
├── 📈 finhub-investment/             # 투자 포트폴리오 서비스
├── 💳 finhub-payment/                # 결제 서비스
├── 🛡️  finhub-insurance/             # 보험 서비스
├── 🔍 finhub-search/                 # Elasticsearch 검색 서비스
├── 🔔 finhub-notification/           # Kafka Consumer 알림 서비스
├── 🤖 finhub-ai/                     # LangGraph AI 어시스턴트 (FastAPI)
│   ├── main.py
│   ├── routers/chat.py
│   ├── services/
│   │   ├── chat_service.py           # LangGraph StateGraph
│   │   ├── rag_service.py            # pgvector RAG
│   │   ├── recommend_service.py      # 상품 추천
│   │   └── tools.py                  # Tool Calling 함수
│   └── core/database.py              # pgvector 연결
│
└── 🖥️  finhub-frontend/              # React SPA (토스 스타일)
    └── src/
        ├── api/                      # Axios 서비스별 API
        ├── components/               # Layout, BottomNav, Modal
        ├── pages/                    # Home, Banking, Investment, AI...
        └── store/                    # Zustand (authStore)
```

---

<div align="center">

**FinHub** — MSA 기반 개인 통합 금융 플랫폼

![Made with Spring Boot](https://img.shields.io/badge/Made_with-Spring_Boot-6DB33F?style=flat-square&logo=springboot)
![Made with FastAPI](https://img.shields.io/badge/Made_with-FastAPI-009688?style=flat-square&logo=fastapi)
![Made with React](https://img.shields.io/badge/Made_with-React-61DAFB?style=flat-square&logo=react)
![Architecture MSA](https://img.shields.io/badge/Architecture-MSA-FF6B6B?style=flat-square)
![Deploy K8s](https://img.shields.io/badge/Deploy-Kubernetes-326CE5?style=flat-square&logo=kubernetes)

</div>
