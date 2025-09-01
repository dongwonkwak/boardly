# Cycle Plan — Boardly

사이클 단위 개발 목표와 운영 방식을 정의한 문서입니다. Epic & Issues, Task Tracker와 동기화하여 관리합니다. Priority와 Labels 정보를 반영합니다.

---

## 사이클 운영 원칙

1. **기간**: 2주 단위 사이클 (Linear Cycle 기준)
2. **목표**: 사이클 시작 시 정의된 Epic & Issue 기반으로 설정
3. **DoD (Definition of Done)**:

   * Backend/Frontend/Integration 작업 완료
   * 단위/통합/E2E 테스트 통과
   * 문서(Task Tracker, Epic & Issues) 업데이트
   * CI/CD 파이프라인 그린 상태
4. **체크인 방식**: 매일 Task Tracker 업데이트, 주 1회 사이클 리뷰

---

## Cycle 1 (MVP 1 일부)

**목표:** 회원가입 + 로그인 기능 완료 (풀스택)

* **Issue #101: 회원가입 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, auth
* **Issue #102: 로그인 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, auth

---

## Cycle 2 (MVP 1 나머지 + MVP 2 시작)

**목표:** Refresh Token 갱신 + 워크스페이스 생성/조회

* **Issue #103: Refresh Token 갱신 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, auth
* **Issue #201: 워크스페이스 생성 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, workspace
* **Issue #202: 워크스페이스 목록/조회 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, workspace

---

## Cycle 3 (MVP 2 계속)

**목표:** 워크스페이스 권한 관리 + 보드 생성/조회/수정/삭제

* **Issue #204: 워크스페이스 멤버 관리 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, workspace, invite
* **Issue #205: 워크스페이스 권한(Role) 관리 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, workspace, rbac
* **Issue #301: 보드 생성 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, board
* **Issue #302: 보드 조회/목록 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, board
* **Issue #303: 보드 수정/삭제 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, board
* **Issue #304: 보드 멤버 관리 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, board, invite
* **Issue #305: 보드 권한(Role) 관리 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, board, rbac

---

## Cycle 4 (MVP 3)

**목표:** 카드/리스트 관리

* **Issue #401: 리스트 생성/수정/삭제 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, list
* **Issue #402: 카드 생성/수정/삭제 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, card
* **Issue #403: 카드 드래그&드롭 이동 처리**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, card, realtime
* **Issue #404: 카드 상세 조회 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, card, comment, file
* **Issue #405: 카드 첨부 파일 업로드/다운로드 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, card, file
* **Issue #406: 카드/리스트 검색 및 필터링 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, card, search
* **Issue #407: 카드 댓글 시스템 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, card, comment

---

## Cycle 5 (MVP 4)

**목표:** 다국어(i18n) 확장 + 알림 시스템

* **Issue #501: 사용자 언어 전환 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n
* **Issue #502: 다국어 리소스 관리 및 배포 전략**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n
* **Issue #503: 에러 메시지 및 도메인 메시지 번역 범위 확대**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n
* **Issue #504: 프론트엔드/백엔드 i18n 동기화 강화**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n
* **Issue #601: 이메일 알림 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, notification, email
* **Issue #602: In-App Notification 기능 구현**

  * Priority: High
  * Labels: backend, frontend, integration, i18n, notification, realtime
* **Issue #603: 알림 설정 관리 기능 구현**

  * Priority: Medium
  * Labels: backend, frontend, integration, i18n, notification

---

## Notes

* Cycle Plan은 Epic & Issues, Task Tracker 문서와 동기화
* Priority와 Labels를 반영해 Linear Cycle 관리에 바로 활용 가능
* 각 Cycle은 Backend/Frontend/Integration이 모두 포함된 풀스택 목표
