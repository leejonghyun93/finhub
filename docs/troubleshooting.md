# FinHub 트러블슈팅 기록

개발 과정에서 마주친 문제와 해결 과정을 기록합니다.

---

## 1. Kubernetes init 컨테이너 — nc 명령어 실패

**환경**: Kubernetes (minikube), busybox 이미지

### 문제

서비스 기동 시 init 컨테이너에서 Kafka·Redis 연결 확인을 위해 여러 방법을 시도했으나 모두 실패.

```bash
# 시도 1 — bitnami/kafka 이미지에 해당 스크립트 없음
kafka-broker-api-versions.sh --bootstrap-server kafka:9092
# Error: command not found

# 시도 2 — /dev/tcp 는 busybox sh에서 미지원
bash -c "echo > /dev/tcp/kafka/9092"
# Error: sh: bad substitution

# 시도 3 — busybox nc에는 -z 플래그 없음
nc -z kafka 9092
# Error: nc: invalid option -- z
```

### 원인

busybox의 `nc`는 GNU netcat과 달리 `-z` (port scan only) 플래그를 지원하지 않는다.
Kafka는 TCP handshake는 되지만 프로토콜 핸드셰이크 완료 전까지 실제 준비 상태를 외부에서 확인할 수 없다.

### 해결

Kafka 특성을 인정하고 **고정 sleep 방식**으로 전환.
Zookeeper가 먼저 올라오면 Kafka도 뒤따라 준비된다는 기동 순서를 활용.

```yaml
# banking.yaml, investment.yaml, payment.yaml, notification.yaml
- name: wait-kafka
  image: busybox:1.36
  command: ["/bin/sh", "-c", "echo 'Waiting for Kafka 40s...'; sleep 40; echo 'Done'"]

# kafka.yaml
- name: wait-zookeeper
  image: busybox:1.36
  command: ["/bin/sh", "-c", "echo 'Waiting for Zookeeper 20s...'; sleep 20; echo 'Done'"]
```

### 배운 점

모든 서비스에 동일한 헬스체크 패턴을 강제 적용하려 했던 것이 문제였다.
서비스 특성에 맞는 방식(TCP 체크 가능한 것은 `pg_isready`/`redis-cli`, 그렇지 않은 것은 sleep)을 선택해야 한다.

---

## 2. minikube OOMKilled — Spring Boot 서비스 메모리 초과

**환경**: minikube (기본 메모리 2048Mi), 18개 컴포넌트 배포

### 문제

전체 서비스를 배포하자 Spring Boot 서비스들이 `OOMKilled`로 반복 재시작.

```
NAME                 STATUS             RESTARTS
finhub-banking       CrashLoopBackOff   5          OOMKilled
finhub-user          CrashLoopBackOff   4          OOMKilled
finhub-investment    CrashLoopBackOff   3          OOMKilled
```

### 원인

JVM은 기본적으로 호스트(컨테이너) 메모리의 25%를 Heap 최대치로 설정한다.
K8s 파드 `limits.memory`가 없으면 JVM이 노드 전체 메모리를 기준으로 힙 크기를 계산,
9개 Spring Boot 서비스가 각각 수백 MB를 확보하려다 노드 메모리가 고갈됐다.

### 해결

```yaml
# helm/finhub/values.yaml
env:
  - name: JAVA_OPTS
    value: "-Xms256m -Xmx512m"   # 힙 명시적 제한

resources:
  requests:
    memory: "256Mi"
    cpu: "200m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

- 모든 Spring Boot 서비스에 `-Xms256m -Xmx512m` 적용
- minikube 권장 사양: `--memory=8192 --cpus=4`

### 배운 점

컨테이너 환경에서 JVM은 컨테이너 메모리 제한을 인식하지 못하면 호스트 전체 메모리 기준으로 동작한다.
`-Xmx` 명시 + K8s `resources.limits` 병행 설정이 필수다.

---

## 3. AI 서비스 — PostgreSQL 연결 타이밍 이슈

**환경**: finhub-ai (FastAPI + SQLAlchemy + pgvector)

### 문제

`pg_isready` 헬스체크를 통과했음에도 `finhub-ai` 기동 시 pgvector 확장 초기화 에러 발생.

```
# pg_isready는 통과
PostgreSQL ready!

# 하지만 직후 연결 시도 시
sqlalchemy.exc.OperationalError: could not connect to server: Connection refused
```

### 원인

`pg_isready`는 PostgreSQL이 TCP 연결을 수락하는 시점을 감지한다.
그러나 `pgvector` 확장 설치 Job이 실행되는 동안 PostgreSQL 내부 초기화가 아직 완료되지 않아,
TCP 포트는 열려 있지만 실제 쿼리를 처리할 수 없는 짧은 간격이 존재한다.

### 해결

`pg_isready` 확인 후 10초 추가 대기.

```yaml
# helm/finhub/templates/ai.yaml
- name: wait-postgres
  image: postgres:15-alpine
  command: ["/bin/sh", "-c",
    "until pg_isready -h {{ .Release.Name }}-postgres -p {{ .Values.infrastructure.postgres.port }};
     do echo 'Waiting for PostgreSQL...'; sleep 2; done;
     echo 'PostgreSQL ready, waiting 10s...'; sleep 10"]
```

### 배운 점

**헬스체크 통과 ≠ 서비스 완전 준비 완료.**
TCP 포트 오픈과 실제 서비스 가용성 사이에는 간격이 존재한다.
특히 확장(Extension) 설치나 마이그레이션이 포함된 서비스는 추가 버퍼 시간이 필요하다.

---

## 4. minikube 반복 다운 — ELK 스택 메모리 초과

**환경**: minikube, Kibana + Logstash + Filebeat 사이드카 추가 시도

### 문제

ELK 스택을 추가하고 `helm upgrade` 실행 중 minikube API 서버가 응답 불능 상태가 됨.

```
Error: UPGRADE FAILED: TLS handshake timeout

$ kubectl get nodes
The connection to the server localhost:8080 was refused — did you specify the right host or port?
```

### 원인

minikube 기본 메모리(2048Mi)를 크게 초과하는 리소스 요청이 발생.

| 컴포넌트 | 메모리 요청 |
|---|---|
| Java 서비스 9개 × 256Mi | ~2,304Mi |
| Kibana | 1,024Mi |
| Logstash | 512Mi |
| Filebeat 사이드카 9개 × 64Mi | ~576Mi |
| **합계** | **~4,416Mi** |

Pod 스케줄링 불가 → Node Pressure → kubelet 응답 중단 → API 서버 연결 끊김.

### 해결

1. ELK 스택 전체 철회 (kibana.yaml, logstash.yaml, filebeat-config.yaml 삭제, 각 서비스 yaml의 filebeat 사이드카 제거)
2. Prometheus + Grafana로 모니터링 일원화 (동일 기능을 수분의 1 리소스로 제공)
3. minikube 재시작 후 재배포

```bash
minikube stop && minikube start --memory=8192 --cpus=4
helm upgrade finhub ./helm/finhub -n finhub --timeout 30m
```

### 배운 점

단일 노드 로컬 클러스터에서 ELK 풀스택 운영은 현실적으로 불가능하다.
모니터링 도구 선택 시 실제 배포 환경의 리소스 제약을 반드시 고려해야 한다.
개발/로컬 환경에는 Prometheus + Grafana, 프로덕션은 별도 로그 클러스터로 분리하는 것이 올바른 설계다.

---

## 5. JwtTokenProviderTest — 생성자 시그니처 불일치

**환경**: finhub-user, JUnit5 + Mockito

### 문제

`JwtTokenProviderTest`에서 기본 생성자(`new JwtTokenProvider()`)로 인스턴스를 생성한 뒤
`ReflectionTestUtils.setField()`로 필드를 주입했으나, 실제 `JwtTokenProvider` 클래스에는 기본 생성자가 없어 컴파일 에러 발생.

```java
// 잘못된 코드
jwtTokenProvider = new JwtTokenProvider();                          // 기본 생성자 없음 — 컴파일 에러
ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", ...);  // 필드명도 실제와 다름
```

실제 클래스 생성자:
```java
public JwtTokenProvider(
    @Value("${jwt.secret}") String secret,
    @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
    @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
) { ... }
```

### 원인

AI(Claude Code)가 테스트 코드를 작성할 때 실제 프로덕션 코드의 생성자 시그니처를 정확하게 반영하지 못했다.
`@Value` 기반 생성자 주입 클래스는 기본 생성자가 없으므로 반드시 실제 생성자를 직접 호출해야 한다.

### 해결

```java
// 수정 후 — 실제 생성자를 직접 호출
@BeforeEach
void setUp() {
    jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
            1800000L,   // 30분
            604800000L  // 7일
    );
}
```

`import org.springframework.test.util.ReflectionTestUtils` 및 관련 코드 전체 제거.

### 배운 점

**AI가 생성한 테스트 코드도 반드시 컴파일·실행으로 검증해야 한다.**
특히 생성자 주입 클래스(`@Value`, `@RequiredArgsConstructor`)는 기본 생성자가 없으므로,
테스트에서 인스턴스를 생성할 때 실제 생성자 시그니처를 직접 확인해야 한다.
`ReflectionTestUtils`는 기본 생성자가 있는 경우에만 유효한 패턴이다.

---

## 요약

| # | 문제 | 핵심 원인 | 해결책 |
|---|------|----------|--------|
| 1 | nc -z 명령어 실패 | busybox nc의 플래그 제한 | sleep 기반 고정 대기로 전환 |
| 2 | Spring Boot OOMKilled | JVM 힙 미제한 | `-Xmx512m` + K8s resources.limits |
| 3 | PostgreSQL 타이밍 이슈 | 헬스체크 통과 ≠ 완전 준비 | pg_isready 후 sleep 10 추가 |
| 4 | minikube API 서버 다운 | ELK 스택 메모리 초과 | ELK 제거 → Prometheus+Grafana |
| 5 | JwtTokenProvider 생성자 불일치 | AI 생성 코드 검증 누락 | 실제 생성자 시그니처 확인 후 수정 |
