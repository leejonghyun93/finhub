# FinHub 💰

<div align="center">

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=for-the-badge&logo=typescript&logoColor=white)

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.6.0-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.13-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**MSA 기반 개인 통합 금융 플랫폼**

토스(Toss) 스타일의 UI로 뱅킹, 투자, 결제, 보험을 하나의 앱에서 관리하는 마이크로서비스 아키텍처 금융 플랫폼입니다.

</div>

---

## 📌 Project Overview

**FinHub**는 Spring Cloud 기반의 MSA 아키텍처로 구성된 개인 통합 금융 플랫폼입니다.
8개의 독립적인 마이크로서비스가 Eureka 서비스 디스커버리와 API Gateway를 통해 유기적으로 연결되며,
Kafka 기반 이벤트 드리븐 아키텍처로 서비스 간 비동기 통신을 구현합니다.

### 🎯 주요 특징

- **MSA 아키텍처** — 8개 도메인 서비스의 독립적 배포 및 확장 가능
- **이벤트 드리븐** — Kafka를 통한 느슨한 결합의 비동기 이벤트 처리
- **JWT 인증** — Access Token(30분) + Refresh Token(7일) + Redis 세션 관리
- **풀텍스트 검색** — Elasticsearch 기반 금융 상품 통합 검색
- **토스 스타일 UI** — React 18 + TypeScript + Tailwind CSS 모바일 우선 설계

---

## ✨ Features

### 🏦 Banking (뱅킹)
- 입출금 계좌 개설 및 관리
- 실시간 계좌 잔액 조회
- 계좌 간 송금 처리
- 잔액 충전
- 거래내역 페이징 조회

### 📈 Investment (투자)
- 주식 종목 목록 조회 및 실시간 가격
- 포트폴리오 생성 및 관리
- 주식 매수 / 매도 실행
- 보유 종목 수익률 계산 (평가금액, 손익, 수익률)
- 매매 내역 조회

### 💳 Payment (결제)
- 카드 등록 및 결제 수단 관리
- 결제 처리
- 결제 내역 조회

### 🛡️ Insurance (보험)
- 보험 상품 목록 및 상세 조회
- 보험 가입 / 해지
- 가입 내역 관리

### 🔍 Search (검색)
- Elasticsearch 기반 금융 상품 통합 검색
- 카테고리별 필터링 (주식, 보험, 계좌)
- 페이징 지원

### 🔔 Notification (알림)
- Kafka 이벤트 기반 실시간 알림 수신
- 알림 읽음 처리 / 전체 읽음
- 알림 삭제

---

## 🛠️ Tech Stack

### Backend
| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Cloud | Spring Cloud 2023.0.2 |
| Build | Maven (Multi-Module) |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway |
| API Docs | RESTful API |

### Frontend
| 분류 | 기술 |
|------|------|
| Framework | React 18 + TypeScript 5.2 |
| Build | Vite 5.3 |
| Styling | Tailwind CSS 3.4 |
| State | Zustand 4.5 |
| Routing | React Router v6 |
| HTTP | Axios 1.7 |
| Toast | React Hot Toast |

### Infrastructure
| 분류 | 기술 | 용도 |
|------|------|------|
| Database | PostgreSQL 16 | 서비스별 독립 DB (6개) |
| Cache | Redis 7 | JWT 세션, 토큰 블랙리스트 |
| Message Queue | Apache Kafka 7.6.0 | 서비스 간 이벤트 통신 |
| Search Engine | Elasticsearch 8.13 | 풀텍스트 검색 |
| Container | Docker + Docker Compose | 로컬 인프라 구성 |
| Registry | Netflix Eureka | 서비스 디스커버리 |
| Gateway | Spring Cloud Gateway MVC | API 라우팅, 인증 |

---

## 🏗️ System Architecture

```
                         ┌─────────────────────────────────┐
                         │         Client (Browser)         │
                         │   React 18 + TypeScript (3000)   │
                         └────────────────┬────────────────┘
                                          │ HTTP /api/v1/**
                         ┌────────────────▼────────────────┐
                         │         API Gateway              │
                         │    Spring Cloud Gateway (8080)   │
                         │    JWT 검증 · 로드밸런싱 · 라우팅  │
                         └──────────────┬──────────────────┘
                                        │
                 ┌──────────────────────┼──────────────────────┐
                 │          Eureka Service Registry (8761)      │
                 └──┬────────┬────────┬──────┬──────┬──────────┘
                    │        │        │      │      │
          ┌─────────▼──┐ ┌───▼───┐ ┌──▼──┐ ┌▼────┐ ┌▼──────────┐
          │finhub-user │ │banking│ │invest│ │pay  │ │ insurance │
          │   (8081)   │ │(8082) │ │(8083)│ │(8084)│ │  (8085)   │
          └─────────┬──┘ └───┬───┘ └──┬──┘ └┬────┘ └──────┬────┘
                    │        │        │      │             │
          ┌─────────▼──┐     │    ┌───▼──────▼─────────────▼────┐
          │finhub-     │     │    │        Apache Kafka           │
          │notification│◄────┘    │  banking.transfer.completed   │
          │   (8088)   │◄─────────│  payment.completed            │
          └────────────┘          │  investment.trade.completed   │
                                  │  insurance.subscribed         │
          ┌─────────────┐         │  user.registered              │
          │finhub-search│         └──────────────────────────────┘
          │   (8086)    │
          └──────┬──────┘
                 │
    ┌────────────▼──────────────────────────────┐
    │              Infrastructure                │
    │  PostgreSQL(5432) · Redis(6379)            │
    │  Kafka(9092) · Elasticsearch(9200)         │
    └────────────────────────────────────────────┘
```

---

## 🔧 MSA Service Configuration

| 서비스 | 포트 | 설명 | DB |
|--------|------|------|-----|
| `finhub-eureka` | 8761 | Eureka Service Registry | - |
| `finhub-gateway` | 8080 | API Gateway (진입점) | - |
| `finhub-user` | 8081 | 사용자 관리 · JWT 인증 | finhub_user |
| `finhub-banking` | 8082 | 계좌 · 거래 관리 | finhub_banking |
| `finhub-investment` | 8083 | 투자 포트폴리오 | finhub_investment |
| `finhub-payment` | 8084 | 결제 처리 | finhub_payment |
| `finhub-insurance` | 8085 | 보험 상품 관리 | finhub_insurance |
| `finhub-search` | 8086 | Elasticsearch 검색 | - |
| `finhub-notification` | 8088 | Kafka 알림 소비자 | finhub_notification |

**기동 순서**: `eureka` → `gateway` → 도메인 서비스

---

## 📨 Kafka Event Flow

```
finhub-user        ──[user.registered]──────────────────────► finhub-notification
finhub-banking     ──[banking.transfer.completed]────────────► finhub-notification
finhub-payment     ──[payment.completed]─────────────────────► finhub-notification
finhub-investment  ──[investment.trade.completed]────────────► finhub-notification
finhub-insurance   ──[insurance.subscribed]──────────────────► finhub-notification
```

| 토픽 | 발행 서비스 | 내용 |
|------|-----------|------|
| `user.registered` | finhub-user | 신규 회원가입 이벤트 |
| `banking.transfer.completed` | finhub-banking | 송금 완료 (출금/입금 계좌, 금액, 시각) |
| `payment.completed` | finhub-payment | 결제 완료 (가맹점, 금액) |
| `investment.trade.completed` | finhub-investment | 매매 완료 (종목, 유형, 수량, 금액) |
| `insurance.subscribed` | finhub-insurance | 보험 가입 완료 |

**Consumer Group**: `notification-group`

---

## 🚀 Getting Started

### Prerequisites

- Docker Desktop 설치
- Node.js 18+ (프론트엔드 개발 시)
- Java 17+ (백엔드 개발 시)

### ▶️ Docker Compose로 실행 (권장)

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/finhub.git
cd finhub

# 2. 인프라 + 전체 서비스 실행
docker-compose up -d

# 3. 서비스 상태 확인
docker-compose ps
```

```bash
# 개별 서비스 재빌드
docker build -f finhub-banking/Dockerfile -t finhub-banking:latest .
docker-compose up -d --build finhub-banking
```

### 🛠️ 개발 환경 실행

#### 1. 인프라 실행

```bash
# PostgreSQL, Redis, Kafka, Elasticsearch만 실행
docker-compose up -d postgres redis zookeeper kafka elasticsearch
```

#### 2. 백엔드 서비스 실행

```bash
# 전체 빌드
./mvnw clean install -DskipTests

# 기동 순서에 따라 실행
./mvnw spring-boot:run -pl finhub-eureka
./mvnw spring-boot:run -pl finhub-gateway

# 도메인 서비스 (별도 터미널)
./mvnw spring-boot:run -pl finhub-user
./mvnw spring-boot:run -pl finhub-banking
./mvnw spring-boot:run -pl finhub-investment
./mvnw spring-boot:run -pl finhub-payment
./mvnw spring-boot:run -pl finhub-insurance
./mvnw spring-boot:run -pl finhub-search
./mvnw spring-boot:run -pl finhub-notification
```

#### 3. 프론트엔드 실행

```bash
cd finhub-frontend
npm install
npm run dev
# http://localhost:3000
```

### 🔗 접속 URL

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |

---

## 📡 API Specification

> 모든 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

### 🔐 User Service
```
POST   /api/v1/users/signup          회원가입
POST   /api/v1/users/login           로그인 → { accessToken, refreshToken }
POST   /api/v1/users/logout          로그아웃
POST   /api/v1/users/reissue         토큰 재발급
GET    /api/v1/users/me              내 정보 조회 🔒
```

### 🏦 Banking Service
```
POST   /api/v1/banking/accounts                          계좌 개설 🔒
GET    /api/v1/banking/accounts                          계좌 목록 🔒
GET    /api/v1/banking/accounts/{accountId}              계좌 상세 🔒
POST   /api/v1/banking/accounts/{accountId}/deposit      잔액 충전 🔒
POST   /api/v1/banking/accounts/transfer                 송금 🔒
GET    /api/v1/banking/accounts/{accountId}/transactions 거래내역 🔒 (페이징)
```

### 📈 Investment Service
```
GET    /api/v1/investment/stocks                              종목 목록
GET    /api/v1/investment/stocks/{stockId}                    종목 상세
POST   /api/v1/investment/portfolios                          포트폴리오 생성 🔒
GET    /api/v1/investment/portfolios                          포트폴리오 목록 🔒
GET    /api/v1/investment/portfolios/{portfolioId}/holdings   보유 종목 🔒
POST   /api/v1/investment/trade                               매매 실행 🔒
GET    /api/v1/investment/trade/history                       매매 내역 🔒 (페이징)
```

### 💳 Payment Service
```
POST   /api/v1/payment/methods                결제 수단 등록 🔒
GET    /api/v1/payment/methods                결제 수단 목록 🔒
DELETE /api/v1/payment/methods/{methodId}     결제 수단 삭제 🔒
POST   /api/v1/payment/pay                    결제 처리 🔒
GET    /api/v1/payment/history                결제 내역 🔒 (페이징)
```

### 🛡️ Insurance Service
```
GET    /api/v1/insurance/products                     보험 상품 목록
GET    /api/v1/insurance/products/{productId}         보험 상품 상세
POST   /api/v1/insurance/subscribe                    보험 가입 🔒
GET    /api/v1/insurance/subscriptions                가입 내역 🔒
DELETE /api/v1/insurance/subscriptions/{id}           보험 해지 🔒
```

### 🔍 Search Service
```
GET    /api/v1/search?keyword=&category=&page=0&size=10   통합 검색 (페이징)
```

### 🔔 Notification Service
```
GET    /api/v1/notification                          알림 목록 🔒 (페이징)
PATCH  /api/v1/notification/{notificationId}/read    읽음 처리 🔒
PATCH  /api/v1/notification/read-all                 전체 읽음 🔒
DELETE /api/v1/notification/{notificationId}         알림 삭제 🔒
```

---

## 📁 Project Structure

```
finhub/
├── 📄 pom.xml                        # Root POM (Spring Boot 3.3.0)
├── 🐳 docker-compose.yml             # 전체 인프라 + 서비스 구성
│
├── 🔭 finhub-eureka/                 # Service Registry
├── 🚪 finhub-gateway/                # API Gateway + JWT 필터
│
├── 👤 finhub-user/                   # 사용자 · 인증 서비스
│   └── src/main/
│       ├── java/com/finhub/user/
│       │   ├── controller/           # UserController
│       │   ├── service/              # UserService (JWT 발급/검증)
│       │   ├── domain/               # User Entity
│       │   ├── dto/                  # LoginRequest/Response, etc.
│       │   ├── security/             # JWT Filter, CustomUserDetails
│       │   └── config/               # SecurityConfig
│       └── resources/db/migration/   # Flyway SQL
│
├── 🏦 finhub-banking/                # 계좌 · 거래 서비스
│   └── src/main/java/com/finhub/banking/
│       ├── controller/               # BankingController
│       ├── service/                  # BankingServiceImpl
│       ├── domain/                   # Account, Transaction (DEPOSIT/WITHDRAWAL/TRANSFER)
│       └── dto/event/                # TransferCompletedEvent (Kafka)
│
├── 📈 finhub-investment/             # 투자 서비스
│   └── src/main/java/com/finhub/investment/
│       ├── controller/               # StockController, PortfolioController, TradeController
│       ├── service/                  # InvestmentServiceImpl
│       └── domain/                   # Stock, Portfolio, Holding, Trade
│
├── 💳 finhub-payment/                # 결제 서비스
├── 🛡️  finhub-insurance/             # 보험 서비스
├── 🔍 finhub-search/                 # Elasticsearch 검색 서비스
├── 🔔 finhub-notification/           # Kafka Consumer 알림 서비스
│
└── 🖥️  finhub-frontend/              # React SPA
    └── src/
        ├── api/                      # axios 인스턴스 + 서비스별 API
        ├── components/               # Layout, BottomNav, TopHeader, Modal
        ├── pages/                    # Home, Banking, Investment, Payment, ...
        └── store/                    # Zustand (authStore)
```

---

## 🔑 Authentication Flow

```
1. POST /api/v1/users/login
        ↓
2. finhub-user → JWT 발급 (AccessToken 30분, RefreshToken 7일)
        ↓
3. Redis에 RefreshToken 저장
        ↓
4. 클라이언트 → localStorage에 AccessToken 저장
        ↓
5. 이후 모든 요청: Authorization: Bearer {accessToken}
        ↓
6. finhub-gateway → JWT 검증 후 라우팅
```

---

## 👨‍💻 Developer

<div align="center">

개발 기간: 2024

**FinHub** — MSA 기반 개인 통합 금융 플랫폼

![Made with Spring Boot](https://img.shields.io/badge/Made_with-Spring_Boot-6DB33F?style=flat-square&logo=springboot)
![Made with React](https://img.shields.io/badge/Made_with-React-61DAFB?style=flat-square&logo=react)
![Architecture MSA](https://img.shields.io/badge/Architecture-MSA-FF6B6B?style=flat-square)

</div>
