# Backend Architecture — Boardly

## 기술 스택

* **Java 21**, **Spring Boot 3.x**
* **Spring Authorization Server**: OAuth2/OIDC (Authorization Code + PKCE)
* **Spring Security 6.x**: 인가, 보안 정책
* **JPA (Hibernate) + PostgreSQL 16**
* **Flyway**: DB 마이그레이션
* **Redis (캐싱 전용)**: 보드 상세, 리스트/카드 목록 캐시
* **H2 Database**: 로컬/테스트 인메모리 DB
* **로컬 파일 시스템**: 파일 저장 (MVP, 이후 S3 확장)

---

## 아키텍처 패턴

* **헥사고날 아키텍처 (Ports & Adapters)**
* **DDD (Domain-Driven Design)**
* **Clean Architecture (의존성 역전 원칙)**
* **도메인 이벤트 기반 비동기 처리 (Spring Events)**

---

## 모듈 구조 (Gradle 멀티모듈)

```
boardly/
├── boardly-shared/          # 공용 유틸/에러/DTO/공통 설정
├── boardly-schema/          # SQL 스키마/마이그레이션 관리(Flyway 스크립트)
├── boardly-domain/          # 도메인 모델, 비즈니스 규칙
├── boardly-application/     # 유스케이스, 애플리케이션 서비스
├── boardly-infrastructure/  # JPA, Redis(캐시), 외부 연동
├── boardly-api/             # REST API, SSE 엔드포인트
└── boardly-app/             # Main Application + OAuth2 인증 서버
```

---

## 주요 컴포넌트

* **API 계층 (boardly-api)**: REST API, SSE 이벤트 스트림 제공
* **도메인 계층 (boardly-domain)**: 엔티티, 밸류 오브젝트, 도메인 서비스
* **애플리케이션 계층 (boardly-application)**: 유스케이스 구현, 비즈니스 흐름 제어
* **인프라 계층 (boardly-infrastructure)**: JPA Repository, Redis Cache, Email Service

---

## 실시간 기능

* **SSE (Server-Sent Events)**: 서버→클라이언트 단방향 알림 (워크스페이스/보드 단위)
* 단일 인스턴스 환경에서는 직접 브로드캐스트, 다중 인스턴스 확장은 후순위 고려

---

## 보안 및 인증

* **OAuth2/OIDC (Authorization Code with PKCE)**
* **JWT Access/Refresh Token**: 로테이션, 만료 관리
* **Spring Security 6.x**: 스코프/롤 기반 권한 제어
* **HTTPS 적용**, 보안 헤더(HSTS, X-Content-Type-Options, X-Frame-Options)

---

## 데이터 관리

* **DB 마이그레이션**: Flyway 버저닝
* **캐싱 전략 (Redis)**:

  * 캐시 대상: 보드 메타데이터, 리스트/카드 목록, 워크스페이스별 보드 목록
  * TTL: 30초\~5분 범위
  * 무효화: 생성/수정/삭제 이벤트 발생 시 해당 키 삭제
* **백업/복구**: DB 정기 덤프, 파일 로컬 백업

---

## 테스트 전략 (백엔드 관점)

* **단위 테스트**: 도메인 규칙 검증 (JUnit5, AssertJ)
* **통합 테스트**: @SpringBootTest + Testcontainers(PostgreSQL, Redis)
* **API 테스트**: REST Assured / WebTestClient
* **아키텍처 검증**: ArchUnit으로 계층 의존성 체크

---

## 성능 및 확장성

* **응답 시간**: API p95 < 500ms 목표
* **동시 사용자**: 50+ (MVP 기준)
* **확장성**: Stateless 애플리케이션, Redis 캐시 활용, 오토스케일링 대비
* **Virtual Thread 활용**: I/O 바운드 API 요청 및 이벤트 처리에 적용해 동시성 극대화
* **캐시 지표 수집**: Redis 히트/미스, 키 수, 메모리 사용량 모니터링

---

## 기술적 의사결정 요약

* **Spring Authorization Server + OAuth2/OIDC**: 표준 기반 인증
* **SSE only**: 단순 구현, 알림/활동 피드 전달
* **Redis 캐시 전용**: 조회 성능 개선, 이벤트 브로커는 후순위
* **멀티모듈 구조**: 도메인/애플리케이션/인프라 계층 명확 분리
* **현실적 단순화**: S3, Redis Pub/Sub, 고급 모니터링은 차후 확장
