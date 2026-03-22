# 💰 FinHub — MSA 기반 개인 통합 금융 플랫폼

---

## 📌 프로젝트 개요

> **한 줄 소개**
> Spring Cloud MSA + LangGraph AI 에이전트 + Kubernetes로 구현한
> 토스(Toss) 스타일의 개인 통합 금융 플랫폼

| 항목 | 내용 |
| --- | --- |
| **개발 기간** | 2024 |
| **개발 인원** | 1인 (풀스택) |
| **서비스 규모** | 마이크로서비스 10개 + 인프라 8개 = 총 18개 컴포넌트 |
| **GitHub** | [github.com/leejonghyun93/finhub](https://github.com/leejonghyun93/finhub) |

### 핵심 기술 스택

| 영역 | 기술 |
| --- | --- |
| **Backend** | Java 17, Spring Boot 3.3.0, Spring Cloud 2023.0.2 |
| **AI Service** | Python 3.11, FastAPI, LangGraph, Ollama (llama3.2), pgvector |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Zustand |
| **Database** | PostgreSQL 16 + pgvector, Redis 7 |
| **Message Queue** | Apache Kafka (Confluent 7.6.0) |
| **Search** | Elasticsearch 8.13 |
| **Orchestration** | Kubernetes (minikube), Helm Chart |
| **Monitoring** | Prometheus, Grafana |
| **CI/CD** | GitHub Actions |

---

## 🏆 구현 성과

### 1. MSA 10개 서비스 설계 및 구현

Spring Cloud 기반 마이크로서비스 아키텍처를 **혼자서 처음부터 설계하고 전체 구현**했습니다.

| 서비스 | 포트 | 핵심 구현 |
| --- | --- | --- |
| finhub-eureka | 8761 | Eureka 서비스 레지스트리 |
| finhub-gateway | 8080 | API Gateway + JWT 필터 + 동적 라우팅 |
| finhub-user | 8081 | JWT 발급/검증, Redis 세션, Refresh Token 재발급 |
| finhub-banking | 8082 | 계좌 개설, 송금, 거래내역 페이징 |
| finhub-investment | 8083 | 포트폴리오 관리, 주식 매매, 수익률 계산 |
| finhub-payment | 8084 | 결제 수단 등록, 결제 처리 |
| finhub-insurance | 8085 | 보험 상품 조회, 가입/해지 |
| finhub-search | 8086 | Elasticsearch 풀텍스트 통합 검색 |
| finhub-ai | 8087 | LangGraph AI 어시스턴트 (Python FastAPI) |
| finhub-notification | 8088 | Kafka Consumer, 실시간 알림 |

**주요 수치**
- 전체 REST API **42개** 엔드포인트 구현
- Flyway 마이그레이션으로 **6개 데이터베이스** 스키마 버전 관리
- Kafka **5개 토픽** 이벤트 드리븐 아키텍처 (banking, payment, investment, insurance, user)
- JWT AccessToken(30분) + RefreshToken(7일) + Redis 토큰 블랙리스트 보안 구현

---

### 2. LangGraph AI 금융 어시스턴트 구현

단순 챗봇이 아닌 **의도를 분류하고 전문 에이전트가 처리하는 멀티 에이전트 시스템**을 구현했습니다.

```
사용자 입력
    ↓
[의도 분류 노드] — LLM (temperature=0.0) 로 5가지 의도 분류
    ↓           ↓           ↓           ↓
계좌조회    소비분석    상품추천    일반상담
 에이전트   에이전트    에이전트    에이전트
    ↓           ↓           ↓
  Tool       Tool      pgvector RAG
 Calling    Calling    유사도 검색
    ↓           ↓           ↓
        최종 응답 { response, intent, tools_used }
```

**구현 포인트**
- `StateGraph` 기반 워크플로우 — 각 노드가 상태를 전달하며 흐름 제어
- `MemorySaver`로 대화 히스토리를 세션별로 유지
- **pgvector** + `nomic-embed-text` 임베딩으로 금융 상품 RAG 구현 (768차원 벡터)
- Tool Calling으로 실제 banking/investment API를 AI가 직접 호출
- 의도 분류 5가지: `ACCOUNT_INQUIRY`, `SPENDING_ANALYSIS`, `PRODUCT_SEARCH`, `INSURANCE`, `GENERAL`

---

### 3. Kubernetes + Helm 배포 자동화

로컬 Docker Compose에서 끝내지 않고 **실제 운영 환경에 가까운 Kubernetes 배포까지 구현**했습니다.

```
helm/finhub/
├── values.yaml          # 단일 파일로 전체 18개 컴포넌트 설정 관리
└── templates/           # 20개 K8s 리소스 템플릿
    ├── initdb-job.yaml  # DB 초기화 Job (멱등성 보장)
    ├── postgres.yaml    # StatefulSet + PVC
    ├── elasticsearch.yaml
    └── (각 서비스 Deployment + Service)
```

**구현 포인트**
- `initContainers`로 의존성 순서 보장 — Postgres 준비 완료 후 앱 기동
- `pg_isready`, `redis-cli ping`, `wget` 헬스체크로 각 인프라 상태 검증
- `StatefulSet + PersistentVolumeClaim`으로 PostgreSQL, Elasticsearch 데이터 영속성 확보
- `NodePort`로 minikube 외부 접근 (Gateway: 30080, Grafana: 30030)
- `ConfigMap + Secret`으로 환경변수와 민감정보 분리 관리
- `helm upgrade` 한 줄로 전체 서비스 무중단 업데이트

---

### 4. Prometheus + Grafana 모니터링 대시보드

**모든 Spring Boot 서비스에 Micrometer 메트릭을 추가**하고, Grafana에서 한눈에 확인할 수 있는 대시보드를 구성했습니다.

| 패널 | 메트릭 | 목적 |
| --- | --- | --- |
| RPS | `rate(http_server_requests_seconds_count[1m])` | 초당 요청 수 모니터링 |
| 응답시간 | `rate(http_server_requests_seconds_sum[1m])` | 지연 이상 조기 감지 |
| JVM Heap | `jvm_memory_used_bytes{area="heap"}` | 메모리 누수 탐지 |
| 스레드 수 | `jvm_threads_live_threads` | 스레드 경합 확인 |
| 에러율 | 5xx 응답 비율 | 장애 알람 기준 |
| CPU | `process_cpu_usage` | 과부하 서비스 식별 |

- Prometheus ConfigMap으로 **8개 서비스** 스크레이프 자동 설정
- Grafana datasource + dashboard ConfigMap으로 **배포 즉시 대시보드 자동 프로비저닝**

---

### 5. 단위 테스트 코드 작성 (Java + Python)

외부 의존성 없이 비즈니스 로직만 검증하는 **순수 단위 테스트**를 작성했습니다.

| 테스트 파일 | 케이스 수 | 검증 내용 |
| --- | --- | --- |
| `JwtTokenProviderTest` | 6 | AccessToken/RefreshToken 생성·검증·위조 토큰 거부 |
| `UserServiceTest` | 11 | 회원가입·로그인·로그아웃·토큰 재발급·내 정보 조회 |
| `BankingServiceTest` | 8 | 계좌 개설·입금·송금 위임·거래내역 페이징 |
| `TransferSagaServiceTest` | 5 | Saga 성공/실패 Kafka 토픽 순서·보상 트랜잭션 |
| `test_chat_service.py` | 11 | 의도 분류·chat()·세션 히스토리 (graph 모킹) |
| `test_rag_service.py` | 7 | embed_query·search·build_context |
| **합계** | **48** | |

**구현 포인트**
- Java: `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` + `@Mock` BDDMockito 스타일
- `ArgumentCaptor`로 Kafka 토픽 발행 **순서**까지 검증
- Python: `unittest.mock.patch`로 LangGraph `graph.ainvoke`, `OllamaEmbeddings` 모킹
- 실제 Ollama 서버·DB 없이 순수 로직만 테스트

---

### 6. Choreography-based Saga 패턴 분산 트랜잭션

단순 로컬 트랜잭션으로는 보장할 수 없는 **서비스 간 분산 트랜잭션**을 Saga 패턴으로 해결했습니다.

```
[송금 요청]
BankingServiceImpl.transfer()
        ↓ 위임
TransferSagaService.executeTransfer()
        │
        ├─ 1. transfer.initiated 발행 (Kafka)
        │
        ├─ 2. 계좌 조회 · 잔액 이체 · 거래내역 저장
        │
        ├─ 성공 → banking.transfer.completed 발행 (Kafka)
        │         └─ finhub-notification이 소비 → 알림
        │
        └─ 실패 → transfer.failed 발행 (Kafka) + 예외 재전파
                  └─ handleTransferFailed() 보상 처리 (감사 로그)
```

**구현 포인트**
- `BankingServiceImpl`은 송금 로직을 직접 갖지 않고 `TransferSagaService`에 **위임** — SRP 준수
- 이벤트 3종 설계: `TransferInitiatedEvent` / `TransferCompletedEvent` / `TransferFailedEvent`
- `@Transactional` 범위 내에서 예외 발생 시 DB 롤백 + Kafka 보상 이벤트 이중 보장
- Choreography 방식으로 중앙 오케스트레이터 없이 서비스 간 자율 협력

---

### 7. GitHub Actions CI/CD 파이프라인

PR 생성부터 Docker Hub 이미지 배포까지 **전 과정을 자동화**했습니다.

```
PR 생성 (main/develop)                push → main
        ↓                                    ↓
  ci.yml 자동 실행               cd.yml 자동 실행
        ↓                                    ↓
Maven 빌드 × 9 서비스 (병렬)    Maven 빌드 + Docker 이미지 빌드
        ↓                                    ↓
  단위 테스트 실행              Docker Hub push
        ↓                       {username}/finhub-{service}:latest
 Python flake8 + black           + :sha-xxxxxxx (버전 추적)
        ↓
  CI Pass/Fail 결과 표시
```

- `strategy.matrix`로 9개 서비스 **병렬 빌드** — 빌드 시간 단축
- GitHub Actions **캐시** 활용 (`cache: maven`, Docker layer cache)
- `test-results` artifact 업로드로 실패 원인 추적 가능

---

## 🔧 기술적 도전과 해결

### 도전 1. Kubernetes init 컨테이너 의존성 체크 실패

**문제**
서비스 기동 시 init 컨테이너에서 Kafka 연결 확인에 `nc`(netcat), `/dev/tcp`, `kafka-broker-api-versions.sh` 등을 시도했으나 모두 실패.

```
# 시도 1 — bitnami/kafka 이미지에 해당 스크립트 없음
kafka-broker-api-versions.sh --bootstrap-server ...  # command not found

# 시도 2 — /dev/tcp는 busybox sh에서 지원하지 않음
bash -c "echo > /dev/tcp/kafka/9092"  # sh: bad substitution

# 시도 3 — nc -z 는 busybox에서 -z 플래그 미지원
nc -z kafka 9092  # nc: invalid option -- z
```

**해결**
Kafka의 특성(TCP handshake는 되지만 프로토콜 핸드셰이크 전까지 연결 확인 불가)을 파악하고, Zookeeper → Kafka 순서로 **고정 sleep 방식**으로 전환.

```yaml
- name: wait-kafka
  image: busybox:1.36
  command: ["/bin/sh", "-c", "echo 'Waiting for Kafka 40s...'; sleep 40; echo 'Done'"]
```

**배운 점**: 모든 컨테이너에 동일한 헬스체크 방법을 적용하려 했던 것이 문제. 서비스 특성에 맞는 방식 선택이 중요함.

---

### 도전 2. minikube 메모리 부족으로 인한 서비스 OOMKilled

**문제**
18개 컴포넌트를 기동하자 minikube 노드 메모리가 부족해 Spring Boot 서비스들이 `OOMKilled`로 반복 재시작.

```
# 증상
finhub-banking   CrashLoopBackOff   OOMKilled
finhub-user      CrashLoopBackOff   OOMKilled
```

**해결**

```yaml
# values.yaml — 서비스별 JVM 힙 크기 제한
env:
  - name: JAVA_OPTS
    value: "-Xms256m -Xmx512m"   # 기본 1GB → 512MB로 제한

resources:
  requests:
    memory: "256Mi"
    cpu: "200m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

- 모든 Spring Boot 서비스에 `-Xms256m -Xmx512m` 적용 → 서비스당 약 400MB 절약
- Ollama(LLM)는 `enabled: false` 기본값으로 설정, 외부 Ollama 엔드포인트 연결 방식으로 전환
- minikube `--memory=8192 --cpus=4` 권장 사양 문서화

**배운 점**: 컨테이너 환경에서는 JVM이 호스트 전체 메모리를 사용하려 하므로 반드시 `Xmx` 명시 필요. K8s `resources.limits`와 JVM 힙 크기를 함께 조율해야 함.

---

### 도전 3. AI 서비스 PostgreSQL 연결 타이밍 이슈

**문제**
`finhub-ai` 컨테이너가 기동 시 `pg_isready`로 PostgreSQL 연결을 확인했지만, PostgreSQL이 준비된 직후 바로 연결 시도 시 `pgvector` 확장 초기화 중 에러 발생.

```
# pg_isready는 통과했지만
sqlalchemy.exc.OperationalError: could not connect to server
# PostgreSQL이 connection accept 상태지만 아직 완전히 초기화 안 됨
```

**해결**
`pg_isready` 확인 후 추가 10초 대기를 init 컨테이너에 삽입.

```yaml
- name: wait-postgres
  image: postgres:15-alpine
  command: ["/bin/sh", "-c",
    "until pg_isready -h finhub-postgres -p 5432;
     do echo 'Waiting...'; sleep 2; done;
     echo 'PostgreSQL ready, waiting 10s...'; sleep 10"]
```

**배운 점**: 헬스체크 통과 ≠ 서비스 완전 준비 완료. TCP 포트 오픈과 실제 서비스 가용성 사이에는 간격이 있으며, 크리티컬 서비스는 추가 버퍼 시간이 필요함.

---

### 도전 4. minikube 반복 다운 (ELK 스택 메모리 초과)

**문제**
Kibana + Logstash + 각 서비스별 Filebeat 사이드카를 추가하자, `helm upgrade` 도중 minikube API 서버가 응답 불능이 되면서 배포 실패.

```
Error: UPGRADE FAILED: TLS handshake timeout
$ kubectl get nodes
The connection to the server localhost:8080 was refused
```

**원인 분석**
minikube 기본 메모리(2048Mi)를 훨씬 초과하는 리소스를 요청함.

| 컴포넌트 | 메모리 요청 |
| --- | --- |
| Java 서비스 9개 × 256Mi | ~2,304Mi |
| Kibana | 1,024Mi |
| Logstash | 512Mi |
| Filebeat 사이드카 9개 × 64Mi | ~576Mi |
| **합계** | **~4,416Mi** |

단일 노드 클러스터에서 Pod 스케줄링이 불가능해지자 Node Pressure가 발생, kubelet이 멈추고 API 서버 응답이 끊겼음.

**해결**
1. ELK 스택(Kibana, Logstash, Filebeat 사이드카) 전체 철회
2. Prometheus + Grafana로 모니터링 일원화 — 동일 기능을 수분의 1 리소스로 제공
3. `minikube stop && minikube start` 후 재배포 성공

```bash
# ELK 관련 파일 삭제
rm helm/finhub/templates/kibana.yaml
rm helm/finhub/templates/logstash.yaml
rm helm/finhub/templates/filebeat-config.yaml
# 각 서비스 yaml에서 filebeat 사이드카 컨테이너 블록 제거

helm upgrade finhub ./helm/finhub  # 정상 완료
```

**배운 점**: 단일 노드 로컬 클러스터에서 ELK 풀스택 운영은 현실적으로 불가능함. 모니터링 도구 선택 시 실제 배포 환경의 리소스 제약을 반드시 고려해야 한다. 개발/로컬 환경에는 Prometheus + Grafana, 프로덕션에는 별도 로그 클러스터(ELK)를 분리하는 것이 올바른 접근법.

---

## 🏗️ 아키텍처 다이어그램

```
┌──────────────────────────────────────────────────────────────────────┐
│                       Frontend :3000                                  │
│                  React 18 + TypeScript (토스 스타일 UI)               │
│         뱅킹 · 투자 · 결제 · 보험 · AI채팅 · 검색 · 알림 9개 페이지   │
└──────────────────────────┬───────────────────────────────────────────┘
                           │ HTTP /api/v1/**
┌──────────────────────────▼───────────────────────────────────────────┐
│                    API Gateway :8080 (NodePort 30080)                 │
│         Spring Cloud Gateway MVC — JWT 검증 · 서비스 라우팅           │
│              /api/v1/users → user  /api/v1/ai → ai  ...              │
└──┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────────┘
   │      │      │      │      │      │      │      │      │
   ▼      ▼      ▼      ▼      ▼      ▼      ▼      ▼      ▼
:8761  :8081  :8082  :8083  :8084  :8085  :8086  :8087  :8088
eureka  user  bank  invest  pay   insur  search   ai    notif
              │      │      │      │             │       ▲
              └──────┴──────┴──────┘             │       │
                         │                       │       │
                   Kafka Topics              Tool Call   │
              transfer / payment / trade         │  Kafka Events
              insurance / registered             │       │
                         └───────────────────────┘───────┘

┌──────────────────────────────────────────────────────────────────────┐
│                          Infrastructure                               │
│   PostgreSQL+pgvector :5432   Redis :6379   Kafka :9092              │
│   Elasticsearch :9200         Ollama :11434 (외부 또는 선택적)        │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                    Monitoring & CI/CD                                 │
│   Prometheus :9090  ·  Grafana :3000 (NodePort 30030)                │
│   GitHub Actions: PR → CI (빌드/테스트)  push → CD (Docker Hub)      │
└──────────────────────────────────────────────────────────────────────┘
```

### AI 에이전트 내부 구조

```
사용자 질문 → [intent_classifier_node]
                       │
          ┌────────────┼─────────────┬──────────────┐
          ▼            ▼             ▼              ▼
   account_agent  analysis_agent  product_agent  general_agent
       │               │               │
   get_balance    get_spending    pgvector RAG
   get_history    analysis        (768dim 유사도)
       │               │               │
       └───────────────┴───────────────┘
                        │
              { response, intent, tools_used }
```

---

## 📖 배운 점 / 회고

### 잘 된 점

**1. MSA를 혼자 처음부터 끝까지 경험했다**
Eureka 서비스 디스커버리, Gateway 라우팅, Kafka 이벤트 흐름, Flyway 마이그레이션까지 MSA의 전체 레이어를 직접 구현하면서 각 컴포넌트가 왜 필요한지 실감했습니다. 단순히 코드를 짜는 것이 아니라 서비스 간 통신 방식, 데이터 일관성, 장애 전파 차단 등을 고민하게 됐습니다.

**2. 로컬에서 끝내지 않고 Kubernetes 배포까지 완성했다**
Docker Compose로 개발 환경을 구성하는 것은 상대적으로 쉬웠지만, Helm Chart로 K8s 배포를 완성하면서 init 컨테이너, PVC, ConfigMap/Secret 분리, 헬스체크 등 운영 환경 고려사항을 직접 다루게 됐습니다.

**3. AI 서비스를 단순 API 호출이 아닌 에이전트로 구현했다**
OpenAI API 단순 호출 수준에서 벗어나, LangGraph로 의도 분류 → 전문 에이전트 라우팅 → Tool Calling → RAG까지 에이전트 설계 전반을 경험했습니다.

---

### 아쉬운 점 / 개선하고 싶은 것

**1. 서비스 간 분산 트랜잭션 처리 미흡**
현재 송금 시 banking 서비스 내에서 트랜잭션을 처리하지만, 실제 MSA에서는 Saga 패턴 또는 2PC가 필요합니다. 향후 Choreography 기반 Saga 패턴을 적용해 볼 계획입니다.

**2. 테스트 코드 부족**
빠른 기능 구현에 집중하다 보니 단위 테스트와 통합 테스트 커버리지가 낮습니다. CI에서 테스트가 자동 실행되도록 구조는 갖췄지만, 실제 테스트 케이스를 충분히 작성하지 못했습니다.

**3. API Gateway 인증 로직 개선 여지**
현재 Gateway에서 JWT 검증 후 각 서비스로 User 정보를 헤더로 전달하는 방식인데, 서비스마다 중복으로 JWT를 다시 파싱하는 부분이 있어 개선이 필요합니다.

---

### 이 프로젝트로 얻은 것

| 영역 | 이전 | 이후 |
| --- | --- | --- |
| 백엔드 | 단일 Spring Boot 앱 | MSA 10개 서비스 설계/운영 경험 |
| 인프라 | Docker Compose | Kubernetes + Helm 배포 자동화 |
| AI | API 단순 호출 | LangGraph 멀티 에이전트 + RAG 설계 |
| 모니터링 | 로그 확인 수준 | Prometheus + Grafana 메트릭 대시보드 |
| DevOps | 수동 배포 | GitHub Actions CI/CD 파이프라인 |
| 보안 | 하드코딩 | 환경변수 분리, .gitignore 관리 |
