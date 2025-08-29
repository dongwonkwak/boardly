# Testing Strategy — Boardly

Boardly 프로젝트의 **테스트 원칙·범위·파이프라인**을 정의합니다. 
백엔드는 Java 21 + Spring Boot(멀티모듈, Hexagonal), 프론트는 React(Vite + TypeScript/TSX) 기반입니다.

---

## 0) 목표

* 기능 빠른 개발을 방해하지 않으면서 **품질 가드레일**을 제공
* 스키마/계약/비즈니스 규칙을 **자동 검증**하여 회귀 방지

---

## 1) 테스트 계층 (백엔드)

| 계층          | 목적                           | 기술/도구                                                   | 언제 실패해야 하나             |
| ----------- | ---------------------------- | ------------------------------------------------------- | ---------------------- |
| Unit        | 순수 도메인/유틸 로직 검증              | JUnit 5, AssertJ, Mockito                               | 규칙 위반, 순수 함수 실패        |
| Slice       | Web/Repository/Service 조각 단위 | `@WebMvcTest`, `@DataJpaTest`, Testcontainers(Postgres) | 계층 간 Wiring, JPA 매핑 오류 |
| Integration | 실제 DB 연동 + 스프링 컨텍스트          | SpringBootTest + Testcontainers(Postgres)               | 트랜잭션/쿼리/이벤트 퍼블리싱       |
| Contract    | OpenAPI 스키마와 응답 일치           | openapi-generator, openapi-validator(서버 응답 검증)          | 스키마/타입 불일치             |
| E2E (API)   | 사용자 플로우 단위                   | RestAssured + Testcontainers                            | 인증/인가, 시나리오 실패         |

> **정책**: CI에서 Integration/Contract가 실패하면 머지 불가.

---

## 2) 스키마 & 마이그레이션 검증 (필수)

* **Flyway Validate**: 모든 파이프라인에서 실행 (`flyway:validate`)
* **엔티티-스키마 일치 검증**: `spring.jpa.hibernate.ddl-auto=validate` (test/prod 동일 정책)
* **DB 대상**: Testcontainers(PostgreSQL)로 통일 (로컬/CI 모두)
* **실행 순서**

  1. 컨테이너 DB 기동 → 2) Flyway migrate → 3) Hibernate validate → 4) Repository/Service 테스트

---

## 3) 도메인 ID & 공통 상수

* `boardly-shared`의 **ID Prefix 상수** 사용 여부를 단위/통합 테스트에서 검증
* ULID/Prefix 포맷, 파싱/직렬화 테스트 포함

---

## 4) 예외/에러 응답 규칙 테스트

* `Failure → ErrorResponse` 매핑 단위/통합 테스트
* 에러 구조(05 문서) 준수: `code`, `message(i18n)`, `details`
* Validation 실패 시 `details` 필드(필드명/메시지) 포함 여부 검증

---

## 5) i18n 테스트

* 메시지 소스: `messages.properties`, `ValidationMessages.properties`
* `Accept-Language` 헤더로 다국어 응답 검증 (ko, en 최소)
* 누락 키 검출 테스트(기본 로케일 대비 키 차집합)

---

## 6) 보안/인증 테스트

* OAuth2/OIDC (PKCE) 흐름을 E2E(API)에서 최소 시나리오로 검증
* 보안 헤더/CORS 설정 스냅샷 검증

---

## 7) 프론트엔드 테스트 (React + Vite + TypeScript/TSX)

| 계층          | 목적                  | 도구                                      |
| ----------- | ------------------- | --------------------------------------- |
| Unit        | 순수 유틸/스토어 로직        | Vitest                                  |
| Component   | UI 렌더/상호작용          | React Testing Library + Vitest          |
| Contract    | OpenAPI 클라이언트 타입 일치 | openapi-generator(클라), `tsc`/Zod 스키마 체크 |
| Integration | API 상호작용            | **MSW**로 서버 모킹, OpenAPI 샘플 기반 핸들러 재사용   |
| E2E (웹)     | 주요 사용자 흐름           | Playwright                              |

* **i18n UI**: 언어 토글 시 텍스트/날짜/숫자 포맷 확인
* **접근성(a11y)**: 최소 `aria-*` 속성 및 키보드 내비게이션 체크(선택)

---

## 8) OpenAPI 계약 테스트

* 목적: 서버 응답이 OpenAPI 스펙과 일치하는지 검증

* 방법:
  * spring-cloud-contract 또는 OpenAPI Validator 라이브러리를 활용하여 Controller 레벨 응답을 스키마와 자동 검증

  * CI 파이프라인에서 OpenAPI 문서(openapi.yaml)와 실제 서버 응답을 비교하여 불일치 시 빌드 실패 처리

* 대상:
  * Controller → Service 계층까지의 실제 응답(JSON)과 OpenAPI 스펙 동기화 확인

* 성과 지표:
  * API 변경 시 문서 불일치 자동 검출

* 클라이언트 SDK(OpenAPI Generator 기반)와 서버 간의 계약 일관성 보장
  * 생성된 TS 클라이언트로 컴파일/런타임(개발) 스키마 검증
  * **Prism**(mock server)로 happy-path 계약 리그레션(선택)

---

## 9) 테스트 데이터 전략

* **마이그레이션 기반 시드 금지**: 테스트는 자체 픽스처/팩토리 사용
* **Factory 패턴**: 도메인/엔티티 생성 헬퍼 모듈화 (유효/경계/무효 케이스)
* **고정 시계**: 시간 의존 로직은 Clock 주입, 테스트에서 고정

---

## 10) 커버리지 & 품질 기준

* **백엔드**: Lines ≥ 80%, Branches ≥ 70%
* **프론트**: Statements ≥ 80%
* 변경 파일에 대한 **Diff Coverage**(선택)로 현실적 목표 유지

---

## 11) 속도 최적화

* Testcontainers **Reusability** 활성화 (로컬): `TESTCONTAINERS_RYUK_DISABLED=true`, 재사용 네트워크
* JUnit **parallel** (단, DB 공유 테스트는 순차)
* Gradle 캐시/빌드 스캔, 프론트 `pnpm` 캐시

---

## 12) CI 파이프라인 (GitHub Actions 개요)

1. **Backend**

   * Setup JDK 21 → Gradle 캐시
   * Testcontainers(Postgres) → Flyway migrate → Hibernate validate
   * Unit/Slice/Integration/Contract 실행, 리포트 업로드 (JUnit XML, JaCoCo)
2. **Frontend**

   * Setup Node + pnpm → 의존성 캐시
   * Type check → Vitest → MSW 기반 통합 → Playwright(헤드리스)
   * 커버리지 리포트 업로드
3. **게이트**

   * 커버리지 임계치 미달/계약 불일치/스키마 검증 실패 시 머지 차단

---

## 13) 명명/조직 규칙

* 테스트 파일: `*Test.java`, `*.spec.ts`
* DisplayName: 시나리오(When/Then) 한글 또는 간결 영문
* 모듈별 디렉토리 일관: `domain/application/infrastructure`와 동일한 패키지 경로로 테스트 배치

---

## 14) 로컬 개발 워크플로우

* **백엔드**

  * `./gradlew :boardly-api:test` (단위/슬라이스)
  * `./gradlew :boardly-api:integrationTest` (통합)
* **프론트**

  * `pnpm test` (unit/component)
  * `pnpm test:e2e` (playwright)

