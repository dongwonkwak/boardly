# Boardly — Epics & Issues (Code-First OpenAPI Flow)

> 개인 포트폴리오 1인 개발 프로젝트. 목표는 **빠른 개발 속도**.
> OpenAPI 전략: **백엔드 코드 작성 → OpenAPI 문서 생성 → 프론트 SDK 코드 생성** (code-first, oazapfts 사용)

---

## 0) OpenAPI 전략 (결정사항)

* **방식**: Code-First

  * 백엔드(Spring Boot)에서 컨트롤러/DTO/Validation을 기준으로 **springdoc-openapi**로 스키마 생성
  * 생성된 **openapi.yaml/json**을 아티팩트로 빌드 산출 → 프론트 빌드 파이프라인이 이를 받아 **oazapfts** 로 SDK 생성
* **장점**

  * 백엔드 개발 속도 우선, 구현과 문서의 괴리 최소화
  * 프론트는 타입 안전성 + 스펙 변경시 컴파일 타임 적발
* **테스트**

  * 계약(Contract) 테스트에서 **Controller 응답 ↔ OpenAPI Schema** 검증 (스냅샷/스키마 검증)
* **파이프라인**

  1. 백엔드 빌드 시 openapi.json 생성 & 산출물로 publish
  2. 프론트 빌드 전에 openapi.json을 가져와 **oazapfts** 기반 SDK 재생성 (CI 캐시 활용)

---

## 1) Epic & Issue 목록

### Epic 1. 사용자 인증/인가

* **Issue 1.1**: OAuth2 / OIDC (PKCE) 기반 로그인/회원가입 API 구현
* **Issue 1.2**: Access/Refresh Token 발급 및 회전 (Rotation)
* **Issue 1.3**: 사용자 세션 및 보안 컨텍스트 관리
* **Issue 1.4**: Spring Security 필터 체인 구성
* **Issue 1.5**: 로그인 페이지 & 인증 흐름 (프론트, OpenAPI Client 연동)

### Epic 2. 워크스페이스 관리

* **Issue 2.1**: 워크스페이스 생성/수정/삭제 API
* **Issue 2.2**: 멤버 초대 & 역할(Role) 관리
* **Issue 2.3**: 개인용/팀용 워크스페이스 타입 지원
* **Issue 2.4**: 워크스페이스 목록/대시보드 UI

### Epic 3. 보드(Board) 관리

* **Issue 3.1**: 보드 생성/수정/삭제 API
* **Issue 3.2**: 보드 멤버 관리 (권한 부여/해제)
* **Issue 3.3**: 즐겨찾기/최근 접근(last\_access\_at) 정렬
* **Issue 3.4**: 보드 목록/상세 화면

### Epic 4. 리스트(List) & 카드(Card) 관리

* **Issue 4.1**: 리스트 생성/수정/삭제 API
* **Issue 4.2**: 카드 생성/수정/삭제 API
* **Issue 4.3**: 카드 이동/정렬 API (Drag & Drop)
* **Issue 4.4**: 카드 상세 UI (설명, 담당자, Due Date 등)

### Epic 5. 협업 기능

* **Issue 5.1**: 보드 초대(요청/수락/거절) 흐름
* **Issue 5.2**: 알림(Notification) 기본 (초대/담당 변경 등)
* **Issue 5.3**: 댓글/활동 로그(Activity Log)

### Epic 6. i18n 다국어 지원 (필수)

* **Issue 6.1**: 백엔드 에러 메시지 다국어 지원 (`MessageSource` 적용)
* **Issue 6.2**: 프론트 i18n 세팅 (react-i18next 또는 동급)
* **Issue 6.3**: UI 텍스트 다국어화 (로그인/워크스페이스/보드/카드 등)
* **Issue 6.4**: 에러 메시지 및 시스템 메시지 번역 적용

### Epic 7. API & 테스트

* **Issue 7.1**: **OpenAPI 문서 자동 생성 파이프라인** (springdoc → openapi.json 산출 & 배포)
* **Issue 7.2**: **Contract 테스트** (Controller 응답 ↔ OpenAPI Schema 검증)
* **Issue 7.3**: Repository 단위 테스트 (Testcontainers + PostgreSQL)
* **Issue 7.4**: 프론트엔드 Mocking (MSW 기반)

### Epic 8. 인프라 & 개발환경

* **Issue 8.1**: Flyway 기반 DB 마이그레이션 관리 (V1\_\_baseline.sql)
* **Issue 8.2**: CI/CD (GitHub Actions) 구축
* **Issue 8.3**: 로깅/모니터링 기본 설정

---

## 2) Linear 등록용 (복붙 템플릿)

### Epics

| Epic        | 설명                               |
| ----------- | -------------------------------- |
| 사용자 인증/인가   | OAuth2/OIDC, 토큰, 보안 컨텍스트, 로그인 UI |
| 워크스페이스 관리   | 생성/수정/삭제, 초대/권한, 타입              |
| 보드 관리       | CRUD, 멤버/권한, 즐겨찾기/최근 접근          |
| 리스트 & 카드 관리 | 리스트/카드 CRUD, 이동/정렬, 상세           |
| 협업 기능       | 초대 흐름, 알림, 댓글/활동 로그              |
| i18n (필수)   | 백엔드/프론트 다국어, 에러 메시지              |
| API & 테스트   | openapi 산출/계약테스트, Repo 테스트, MSW  |
| 인프라 & 개발환경  | Flyway, CI/CD, 로깅/모니터링           |

### Issues (샘플: Epic 7)

| Epic      | Issue                  | 설명                                   |
| --------- | ---------------------- | ------------------------------------ |
| API & 테스트 | OpenAPI 문서 자동 생성 파이프라인 | springdoc으로 openapi.json 산출, 아티팩트 배포 |
| API & 테스트 | Contract 테스트           | Controller 응답 ↔ OpenAPI Schema 검증    |
| API & 테스트 | Repository 테스트         | Testcontainers + PostgreSQL          |
| API & 테스트 | 프론트 Mocking            | MSW로 API 모킹 환경                       |

---

## 3) 구현 메모 (실행 가이드)

* **백엔드**

  * 의존성: `org.springdoc:springdoc-openapi-starter-webmvc-ui`
  * 빌드 태스크: `:boardly-api:generateOpenApi`(커스텀) → `openapi.json` 산출 & `build/`에 저장
  * CI에서 아티팩트 업로드 (actions/upload-artifact)
* **프론트**

  * 의존성: `oazapfts`
  * 빌드 전 단계에서 `openapi.json` 다운로드 → `oazapfts` 로 SDK 생성
  * 생성 SDK는 `src/generated/api.ts` (커밋 제외)
  * 스크립트 예시: `"gen:api": "oazapfts ./openapi.json -o src/generated/api.ts"`
* **테스트**

  * 계약 테스트: 스프링 MVC 테스트로 실제 Controller 호출 → 응답 본문을 스키마로 검증 (json-schema-validator)
  * 스냅샷: OpenAPI 스키마 버전 관리 (변경 시 Diff 체크)

---

## 4) 변경 이력

* 2025-08-28: OpenAPI 전략을 **Code-First**로 확정, 완료된 항목 제거 후 Epic/Issue 번호 재정렬
* 2025-08-28: Epic 6을 **필수**로 변경 (백엔드/프론트 다국어 지원 초기부터 적용), Epic 4에서 "컬럼" 용어 제거
* 2025-08-28: 프론트 SDK 생성 방식을 **oazapfts**로 확정
* 2025-08-28: 에러코드 정책 확정 — `code`는 문자열 slug, `message`는 서버 i18n 적용
* 2025-08-28: DB 초기 스키마 & ID 규약 확정 (V1\_\_baseline.sql, prefix+ULID, TIMESTAMPTZ)
* 2025-08-28: 작업 체계 별도 문서로 분리 (Linear, GitHub Flow, PR 자기검증, CI/CD 릴리즈 규칙)
