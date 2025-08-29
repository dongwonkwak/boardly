# Task Tracker — Boardly

Epic & Issue 문서에 정의된 이슈를 실제 개발 가능한 Task 단위로 쪼개어 관리하는 문서입니다. Linear Cycle과 동기화하여 사용합니다. 풀스택 개발(Backend + Frontend + Integration)에 맞춰 구성됩니다.

---

## Cycle 1 (MVP 1 일부)

**목표:** 회원가입 + 로그인 기능 완료 (풀스택)

### Epic 1: 사용자 인증/인가

* **Issue #101: 회원가입 기능 구현**

  ## Backend Tasks

  * Task 1: 회원가입 요청/응답 DTO 정의
  * Task 2: User Entity 및 Repository 정의
  * Task 3: 회원가입 Service/UseCase 구현
  * Task 4: 회원가입 Controller API 구현
  * Task 5: 유효성 검증 로직 추가 (Vavr Validation)
  * Task 6: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 7: 회원가입 페이지 UI 컴포넌트 구현 (Ant Design)
  * Task 8: 회원가입 폼 validation 및 상태 관리
  * Task 9: 회원가입 API 연동 (React Query)
  * Task 10: 에러 처리 및 사용자 피드백 UI
  * Task 11: 프론트엔드 컴포넌트 테스트 작성

  ## Integration Tasks

  * Task 12: API 연동 통합 테스트
  * Task 13: E2E 회원가입 시나리오 테스트 (성공, 중복 이메일, 유효성 실패)

* **Issue #102: 로그인 기능 구현**

  ## Backend Tasks

  * Task 1: 로그인 요청/응답 DTO 정의
  * Task 2: 인증 Service/UseCase 구현
  * Task 3: JWT 토큰 발급 로직 구현
  * Task 4: 로그인 Controller API 구현
  * Task 5: 로그인 실패 시 에러 응답 처리
  * Task 6: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 7: 로그인 페이지 UI 컴포넌트 구현
  * Task 8: 로그인 폼 validation 및 상태 관리
  * Task 9: 로그인 API 연동 및 토큰 저장 로직
  * Task 10: 인증 상태 관리 (Context API)
  * Task 11: 로그인 실패 에러 처리 UI
  * Task 12: 프론트엔드 컴포넌트 테스트 작성

  ## Integration Tasks

  * Task 13: 로그인 후 리다이렉트 로직 테스트
  * Task 14: E2E 로그인 시나리오 테스트 (성공, 실패, 만료 토큰)

---

## Cycle 2 (MVP 1 나머지 + MVP 2 시작)

**후보 목표:** Refresh Token 갱신 + 워크스페이스 생성/조회 (풀스택)

### Epic 1: 사용자 인증/인가

* **Issue #103: Refresh Token 갱신 기능 구현**

  ## Backend Tasks

  * Task 1: Refresh Token 저장소 설계
  * Task 2: Refresh Token 회전 전략 구현
  * Task 3: Access Token 재발급 API 구현
  * Task 4: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 5: 토큰 만료 감지 및 자동 갱신 로직
  * Task 6: API 호출 인터셉터 구현 (토큰 갱신)
  * Task 7: 갱신 실패 시 로그아웃 처리
  * Task 8: 프론트엔드 토큰 관리 테스트 작성

  ## Integration Tasks

  * Task 9: 토큰 갱신 통합 테스트
  * Task 10: E2E 세션 만료 시나리오 테스트 (자동 갱신, 실패 후 로그아웃)

### Epic 2: 워크스페이스 관리

* **Issue #201: 워크스페이스 생성 기능 구현**

  ## Backend Tasks

  * Task 1: Workspace Entity 및 Repository 정의
  * Task 2: 워크스페이스 생성 Service/UseCase 구현
  * Task 3: 워크스페이스 생성 Controller API 구현
  * Task 4: 워크스페이스 권한 검증 로직
  * Task 5: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 6: 워크스페이스 생성 모달/페이지 UI 구현
  * Task 7: 워크스페이스 생성 폼 및 validation
  * Task 8: 워크스페이스 생성 API 연동
  * Task 9: 생성 후 워크스페이스 목록 갱신
  * Task 10: 프론트엔드 컴포넌트 테스트 작성

  ## Integration Tasks

  * Task 11: 워크스페이스 생성 통합 테스트
  * Task 12: E2E 워크스페이스 생성 시나리오 테스트 (생성, 권한 없는 경우 실패)

* **Issue #202: 워크스페이스 목록/조회 기능 구현**

  ## Backend Tasks

  * Task 1: 워크스페이스 조회 Service/UseCase 구현
  * Task 2: 워크스페이스 목록/상세 Controller API 구현
  * Task 3: 페이징 및 정렬 로직 구현
  * Task 4: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 5: 워크스페이스 목록 페이지 UI 구현
  * Task 6: 워크스페이스 카드 컴포넌트 구현
  * Task 7: 워크스페이스 목록 API 연동 (React Query)
  * Task 8: 로딩/에러 상태 UI 처리
  * Task 9: 워크스페이스 상세 조회 페이지 구현
  * Task 10: 프론트엔드 컴포넌트 테스트 작성

  ## Integration Tasks

  * Task 11: 워크스페이스 목록/조회 통합 테스트
  * Task 12: E2E 워크스페이스 네비게이션 테스트 (목록 → 상세 페이지 이동)

---

## Cycle 3 (MVP 2 계속)

**후보 목표:** 워크스페이스 권한 관리 + 보드 생성/조회/수정/삭제 (풀스택)

### Epic 2: 워크스페이스 관리

* **Issue #204: 워크스페이스 멤버 관리 기능 구현**

  ## Backend Tasks

  * Task 1: 워크스페이스 멤버 Entity 및 Repository 정의
  * Task 2: 이메일 초대 Service 구현
  * Task 3: 초대 수락/거절/만료 로직 구현
  * Task 4: 멤버 추가/삭제 Controller API 구현
  * Task 5: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 6: 멤버 초대 모달 UI 구현
  * Task 7: 멤버 목록 관리 페이지 구현
  * Task 8: 초대 링크 생성/공유 기능
  * Task 9: 멤버 관리 API 연동
  * Task 10: 프론트엔드 컴포넌트 테스트 작성

  ## Integration Tasks

  * Task 11: 이메일 초대 플로우 통합 테스트
  * Task 12: E2E 멤버 초대/관리 시나리오 테스트 (초대, 수락, 만료)

* **Issue #205: 워크스페이스 권한(Role) 관리 기능 구현**

  ## Backend Tasks

  * Task 1: 역할 기반 접근 제어 (RBAC) 설계
  * Task 2: 워크스페이스 권한 Service 구현
  * Task 3: 권한 검증 Aspect/Interceptor 구현
  * Task 4: 권한 관리 Controller API 구현
  * Task 5: 백엔드 단위/통합 테스트 작성

  ## Frontend Tasks

  * Task 6: 권한별 UI 표시/숨김 로직 구현
  * Task 7: 멤버 권한 변경 UI 구현
  * Task 8: 권한 관리 API 연동
  * Task 9: 권한 부족 시 에러 처리 UI
  * Task 10: 프론트엔드 권한 테스트 작성

  ## Integration Tasks

  * Task 11: 권한 기반 접근 제어 통합 테스트
  * Task 12: E2E 권한별 접근 시나리오 테스트 (OWNER/MEMBER/GUEST)

### Epic 3: 보드 관리

* **Issue #301: 보드 생성 기능 구현**

  * Backend: 보드 Entity/Service/API 구현 + 단위 테스트
  * Frontend: 보드 생성 UI 및 설정 폼 개발 + 컴포넌트 테스트
  * Integration: 보드 생성 통합 테스트 + E2E 보드 생성 시나리오 (생성, 권한 없는 경우 실패)

* **Issue #302: 보드 조회/목록 기능 구현**

  * Backend: 보드 목록/상세 API 구현 + 단위 테스트
  * Frontend: 보드 목록/상세 페이지 UI 구현 + 컴포넌트 테스트
  * Integration: 보드 조회 통합 테스트 + E2E 보드 목록 → 상세 이동 시나리오

* **Issue #303: 보드 수정/삭제 기능 구현**

  * Backend: 보드 수정/삭제 API 구현 + 단위 테스트
  * Frontend: 보드 설정 UI 및 삭제 확인 UI 개발 + 컴포넌트 테스트
  * Integration: 보드 수정/삭제 통합 테스트 + E2E 보드 삭제 시나리오 (확인/취소)

* **Issue #304: 보드 멤버 관리 기능 구현**

  * Backend: 보드 멤버 초대/추가/삭제 API 구현 + 단위 테스트
  * Frontend: 보드 멤버 관리 UI 개발 + 초대 링크 생성/공유
  * Integration: 멤버 관리 통합 테스트 + E2E 보드 멤버 초대 시나리오

* **Issue #305: 보드 권한(Role) 관리 기능 구현**

  * Backend: 보드 RBAC 시스템 구현 + 권한 검증 API + 단위 테스트
  * Frontend: 권한별 보드 UI 제어 로직 개발 + 컴포넌트 테스트
  * Integration: 권한 통합 테스트 + E2E 권한별 보드 접근 시나리오

---

## Cycle 4 (MVP 3)

**후보 목표:** 카드/리스트 관리 (풀스택)

### Epic 4: 카드/리스트 관리

* **Issue #401: 리스트 생성/수정/삭제 기능 구현**

  * Backend: 리스트 CRUD API + 순서 관리 로직 + 단위 테스트
  * Frontend: 리스트 컴포넌트 UI + 드래그앤드롭 구현 + 컴포넌트 테스트
  * Integration: 리스트 CRUD 통합 테스트 + E2E 리스트 관리 시나리오 (생성/수정/삭제/순서 변경)

* **Issue #402: 카드 생성/수정/삭제 기능 구현**

  * Backend: 카드 CRUD API + 상세 정보 로직 + 단위 테스트
  * Frontend: 카드 컴포넌트 UI + 상세 모달 구현 + 컴포넌트 테스트
  * Integration: 카드 CRUD 통합 테스트 + E2E 카드 관리 시나리오 (생성/수정/삭제)

* **Issue #403: 카드 드래그&드롭 이동 처리**

  * Backend: 카드 이동 API + 리스트 간 이동 로직 + 단위 테스트
  * Frontend: 드래그앤드롭 UI + 상태 관리 + WebSocket 연결 처리 + 컴포넌트 테스트
  * Integration: 카드 이동 통합 테스트 + E2E 드래그앤드롭 시나리오 (실시간 동기화 포함)

* **Issue #404: 카드 상세 조회 기능 구현 (댓글/첨부 포함)**

  * Backend: 카드 상세 조회 API + 댓글/첨부 엔티티 + 업로드 로직(S3/로컬) + 단위 테스트
  * Frontend: 카드 상세 모달 + 댓글/첨부 컴포넌트 구현 + 컴포넌트 테스트
  * Integration: 카드 상세 통합 테스트 + E2E 카드 상세 조회 시나리오 (댓글, 파일 포함)

* **Issue #405: 카드 첨부 파일 업로드/다운로드 기능 구현**

  * Backend: 파일 업로드/다운로드 API + 스토리지 연동 + 단위 테스트
  * Frontend: 파일 첨부 UI + 드래그앤드롭 업로드 구현 + 컴포넌트 테스트
  * Integration: 파일 첨부 통합 테스트 + E2E 파일 업로드/다운로드 시나리오

* **Issue #406: 카드/리스트 검색 및 필터링 기능 구현**

  * Backend: 검색/필터 API (키워드, 라벨, 담당자) + 단위 테스트
  * Frontend: 검색 UI (검색바, 필터 옵션, 자동완성) + 컴포넌트 테스트
  * Integration: 검색 통합 테스트 + E2E 검색/필터링 시나리오

* **Issue #407: 카드 댓글 시스템 구현**

  * Backend: 댓글 CRUD API + 멘션 기능 + 단위 테스트
  * Frontend: 댓글 UI 컴포넌트 (작성, 수정, 삭제) + 컴포넌트 테스트
  * Integration: 댓글 통합 테스트 + E2E 댓글 작성/수정/삭제 시나리오

---

## Cycle 5 (MVP 4)

**후보 목표:** 다국어(i18n) 확장 (풀스택)

### Epic 5: 다국어(i18n) 확장

* **Issue #501: 사용자 언어 전환 기능 구현**

  * Backend: 사용자 언어 설정 API + 다국어 메시지 처리 + 단위 테스트
  * Frontend: 언어 선택 컴포넌트 + React i18n 설정 + 전환 로직 + 컴포넌트 테스트
  * Integration: 언어 전환 통합 테스트 + E2E 언어 전환 시나리오 (한국어 ↔ 영어)

* **Issue #502: 다국어 리소스 관리 및 배포 전략**

  * Backend: 리소스 관리 시스템 + 번역 키 자동 추출 + 버전 관리 + 단위 테스트
  * Frontend: 번역 파일 핫 리로딩 + 미번역 텍스트 감지 + 컴포넌트 테스트
  * Integration: 리소스 관리 통합 테스트 + E2E 번역 리소스 동기화 시나리오

* **Issue #503: 에러 메시지 및 도메인 메시지 번역 범위 확대**

  * Backend: 에러 메시지 국제화 처리 + 단위 테스트
  * Frontend: UI 텍스트 번역 확장 + 컴포넌트 테스트
  * Integration: 메시지 번역 통합 테스트 + E2E 다국어 메시지 표시 시나리오

* **Issue #504: 프론트엔드/백엔드 i18n 동기화 강화**

  * Backend: 번역 키 동기화 API + 누락 감지 + 단위 테스트
  * Frontend: 번역 품질 관리 도구 개발 + 알림 시스템 연동 + 컴포넌트 테스트
  * Integration: i18n 동기화 통합 테스트 + E2E 번역 누락 감지 시나리오

---

## Notes

* Epic & Issues 문서 구조와 동기화하여 업데이트됨
* 각 Issue는 Backend, Frontend, Integration Task로 구분하여 풀스택 개발에 최적화
* Cycle Plan 목표에 따라 Task 세부 분해는 점진적으로 확장
* Linear Cycle과 연계하여 실제 진행 상태 관리
* 드래그앤드롭, WebSocket 실시간 업데이트, 파일 업로드 등 복잡한 기능은 Integration Task 비중 증가
