# Boardly - 전체 시스템 아키텍처

## 프로젝트 개요

* **목적**: 트렐로 유사 칸반 도구 포트폴리오
* **핵심 가치**: 현대적 엔터프라이즈 아키텍처 + 고성능 + 운영 최적화

---

## 전체 시스템 구성

### Backend Architecture

**언어 및 프레임워크**

* **Java 21**: LTS, Virtual Threads, Pattern Matching
* **Spring Boot 3.x**: Spring Framework 6.x
* **Spring Authorization Server**: OAuth2/OIDC 인증 서버

**아키텍처 패턴**

* **헥사고날 아키텍처**: Ports & Adapters 패턴
* **DDD**: Domain-Driven Design 적용
* **이벤트 드리븐**: 도메인 이벤트 기반 비동기 처리
* **Clean Architecture**: 의존성 역전 원칙

**모듈 구조 (Gradle 멀티모듈)**

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

**데이터 저장소**

* **PostgreSQL 16**: 주 데이터베이스
* **H2 Database**: 개발/테스트 환경 (인메모리)
* **Redis**: **캐시 용도(MVP: 보드 상세, 리스트/카드 목록)**
* **로컬 파일 시스템**: 파일 저장소 (S3는 선택적 확장)

**인증 및 보안**

* **Spring Authorization Server**: OAuth2/OIDC 표준 준수
* **Authorization Code with PKCE**: 보안 강화
* **JWT Access/Refresh Token**: 토큰 로테이션 지원
* **Spring Security 6.x**: 인가 및 보안 정책

**실시간 통신**

* **Server-Sent Events (SSE)**: 단방향 실시간 알림 (서버→클라이언트)

  * 워크스페이스/보드 이벤트 알림에 사용
  * Redis Pub/Sub은 MVP에선 사용하지 않음 (스케일아웃 시 확장 고려)

---

### Frontend Architecture

**기술 스택**

* **React 19**: 최신 기능 활용 (Concurrent Features, Suspense)
* **TypeScript 5.x**: 타입 안전성
* **Vite 5.x**: 빠른 개발 서버, HMR

**아키텍처**

* **컴포넌트 기반**: 재사용 가능한 UI 컴포넌트
* **기능별 모듈화**: 도메인별 폴더 구조

**상태 관리**

* **React Query (TanStack Query)**: 서버 상태 관리
* **Context API + useReducer**: 전역 클라이언트 상태
* **Local State (useState)**: 컴포넌트 지역 상태

**UI 및 사용성**

* **Ant Design 5.x**: 엔터프라이즈급 UI 컴포넌트
* **@dnd-kit**: 고성능 드래그앤드롭
* **반응형 디자인**: 모바일 친화적 UI

**API 통신**

* **OpenAPI 3.0 기반**: 스펙 우선 개발
* **자동 생성 클라이언트**: TypeScript 타입 안전성
* **oazapfts**: OpenAPI 기반 HTTP 클라이언트 생성 → React Query와 직접 연동

---

### Infrastructure & DevOps

**컨테이너화 및 배포**

* **Docker**: 애플리케이션 컨테이너화
* **Docker Compose**: 로컬 개발 환경
* **AWS/클라우드**: 선택적 (로컬 배포 우선)

**데이터베이스 및 저장소**

* **PostgreSQL**: 로컬/Docker 또는 관리형 서비스
* **Redis**: 로컬/Docker 기본 (MVP: 캐싱만 사용)
* **로컬 파일 시스템**: 개발/테스트 단계

**CI/CD 파이프라인**

```yaml
GitHub Actions (필수):
  CI:
    - 빌드 및 테스트 실행
    - 코드 품질 검사
    - 테스트 커버리지 리포트
  CD (선택적):
    - Docker 이미지 빌드
    - 로컬 배포 또는 클라우드 배포
```

**관측성 및 모니터링**

* **Spring Actuator**: 애플리케이션 메트릭 및 헬스체크
* **구조화된 로깅**: JSON 형태 로그 출력
* **Micrometer**: 기본 메트릭 (Prometheus 선택적)

---

## 핵심 기능 (MVP)

1. 인증 및 사용자 관리
2. 워크스페이스 관리
3. 보드 관리
4. 칸반 보드 기능
5. 협업 기능 (댓글, 파일 첨부, 라벨)
6. 활동 추적 (로그 + 실시간 알림)
7. 검색 및 필터
8. 초대 및 알림 시스템
9. 대시보드

---

## 현실적 구현 전략

**필수 기능:**

* OAuth2/JWT 인증
* 워크스페이스/보드/카드 CRUD
* 실시간 업데이트 (SSE)
* 파일 업로드 (로컬 저장)
* 이메일 초대 시스템

**선택적 기능:**

* Redis 캐싱 (MVP에 포함, 보드 상세/리스트·카드 목록 캐시)
* 고급 검색
* 활동 로그 상세화
* 푸시 알림

**단순화 전략:**

* WebSocket → SSE (단방향 알림으로 충분)
* Redis Pub/Sub → 미사용 (MVP는 단일 인스턴스 SSE로 처리)
* OpenTelemetry → 기본 로깅
* Grafana → Spring Boot Admin
* S3 → 로컬 파일 시스템

---

## 아키텍처 품질 속성

**성능 요구사항**

* p95 < 500ms
* 동시 사용자 50명 이상
* SSE 지연 < 200ms

**확장성**

* Stateless 애플리케이션
* 외부화된 상태: PostgreSQL, Redis(캐싱)
* 수평 확장 준비 (로드밸런서 대응)

**가용성**

* 목표: 99.5%
* 모니터링: Actuator 헬스체크
* 복구: 기본 에러 처리 및 재시작

**보안**

* OAuth2/OIDC 표준 준수
* JWT 기반 권한 제어
* HTTPS + 보안 헤더
* 민감정보 로깅 금지

---

## 데이터 관리 전략

**캐싱 전략 (MVP: Redis)**

* 대상: 보드 상세, 리스트/카드 목록
* 패턴: Cache-Aside
* TTL: 수 분 단위
* 무효화: 생성/수정/삭제 이벤트 시 정밀 삭제

**데이터 일관성**

* @Transactional 트랜잭션 관리
* Spring Events 비동기 이벤트

**백업 및 복구**

* PostgreSQL 덤프 백업
* 파일 로컬 백업
* 설정은 Git으로 관리

---

## 실시간 아키텍처 (SSE 기반)

**SSE 구현 예시**

```java
@GetMapping(value = "/api/workspaces/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribe(@PathVariable String id) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    hub.add(id, emitter);
    emitter.onCompletion(() -> hub.remove(id, emitter));
    emitter.onTimeout(() -> hub.remove(id, emitter));
    return emitter;
}
```

**이벤트 처리 예시**

```java
@EventListener
public void handleMemberInvited(MemberInvitedEvent event) {
    hub.broadcast(event.getWorkspaceId(), SseEmitter.event()
        .name("member.invited")
        .data(event));
}
```

---

## 테스트 전략

* 단위 테스트: JUnit5, Mockito, AssertJ
* 통합 테스트: SpringBootTest, Testcontainers
* 아키텍처 테스트: ArchUnit
* API 테스트: REST Assured, WebTestClient
* E2E 테스트: Playwright (로그인→대시보드)

커버리지 목표:

* 전체 75%+
* 도메인 90%+
* 핵심 비즈니스 로직 95%+

---

## 배포 및 운영

**개발/테스트**

* Docker Compose (Postgres, Redis, MailHog)
* H2 인메모리 DB
* JAR 실행

**프로덕션 (선택적)**

* PostgreSQL 16
* Docker 배포, 로드밸런싱
* SMTP 서비스 (Gmail/SendGrid)

**운영 도구**

* 로그 관리: 파일 로테이션
* 헬스체크: Actuator
* 모니터링: 기본 JVM/커스텀 메트릭

---

## 확장 로드맵

**Phase 2**

* 고급 검색, 상세 활동 로그
* 모바일 앱
* 외부 서비스 연동(Slack 등)

**Phase 3**

* 엔터프라이즈 기능(SSO, 권한 강화)
* 데이터 거버넌스
* 멀티 리전 배포

---

## 기술적 의사결정 요약

* **Java 21 + Spring Boot 3.x**: LTS + 최신 기능
* **헥사고날 아키텍처 + 멀티모듈**: 계층 분리, 테스트 용이
* **Spring Authorization Server + OAuth2**: 표준 인증/인가
* **PostgreSQL + Redis(캐시만)**: 안정성 + 성능
* **SSE**: 단방향 알림, 구현 단순
* **현실적 단순화**: 로컬 중심, 최소 의존성, 빠른 개발
