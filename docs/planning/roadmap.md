# Roadmap — Boardly

Boardly 프로젝트의 장기 개발 로드맵을 정의합니다. Epic → Issue → Task 단위로 관리되며, Epics & Issues, Development Cycle Plan, Task Tracker와 연결됩니다.

---

## Phase 1: 사용자 인증/인가 (2025 Q3)

* **목표**: 사용자 계정 생성 및 OAuth2 기반 인증 플로우 제공
* **Epic**: 사용자 인증/인가

  * **Issue: 로그인 기능 구현 (BRD-101)**

    * 세부 Task: [Task Tracker](task-tracker.md#issue-로그인-기능-구현-brd-101)
  * **Issue: 회원가입 기능 구현 (BRD-102)**

    * 세부 Task: [Task Tracker](task-tracker.md#issue-회원가입-기능-구현-brd-102)
* **완료 기준**:

  * OAuth2 로그인 리다이렉션 플로우 완료
  * 회원가입 API 및 유효성 검증 구현

---

## Phase 2: 워크스페이스 관리 (2025 Q3\~Q4)

* **목표**: 팀 협업의 기반이 되는 워크스페이스 기능 제공
* **Epic**: 워크스페이스 관리

  * **Issue: 워크스페이스 생성/삭제 (BRD-103)**
  * **Issue: 워크스페이스 멤버 초대 (BRD-104)**
* **완료 기준**:

  * 워크스페이스 CRUD 가능
  * 초대 플로우 동작 (PENDING, ACCEPTED, REJECTED)

---

## Phase 3: 보드 관리 (2025 Q4)

* **목표**: 워크스페이스 내 보드를 생성/삭제 및 멤버 관리 기능 제공
* **Epic**: 보드 관리

  * **Issue: 보드 생성/삭제 (BRD-105)**
  * **Issue: 보드 멤버 관리 (BRD-106)**
* **완료 기준**:

  * 보드 CRUD 가능
  * 멤버 역할(Role) 관리 가능 (OWNER, MEMBER, VIEWER)

---

## Phase 4: 카드/리스트 관리 (2025 Q4 \~ 2026 Q1)

* **목표**: 보드 내의 리스트와 카드 단위 작업 관리 기능 제공
* **Epic**: 카드/리스트 관리

  * **Issue: 리스트 관리 (BRD-107)**
  * **Issue: 카드 관리 (BRD-108)**
* **완료 기준**:

  * 리스트 CRUD 가능
  * 카드 CRUD 가능

---

## Phase 5: i18n 및 UX 개선 (2026 Q1)

* **목표**: 다국어 지원 및 UX 최적화 제공
* **Epic**: i18n 및 UX 개선

  * **Issue: 다국어 지원 MVP (BRD-109)**
* **완료 기준**:

  * 한국어/영어 기본 지원
  * 언어 스위치 플로우 적용
