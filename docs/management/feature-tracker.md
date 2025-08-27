# Feature Tracker — Boardly (Stories × Linear × GitHub)

사용자 스토리 단위로 기능 진행 현황을 관리하는 문서입니다. 각 스토리는 Linear Issue 및 GitHub 브랜치/PR과 연결되며, Epic & Issue, Task Tracker와도 매핑됩니다.

---

## 사용법

1. 새로운 기능을 시작할 때 사용자 스토리 문서를 먼저 작성하고, 여기 표에 링크합니다.
2. 스토리에서 파생된 Linear Issue를 등록하고 연결합니다. (예: BRD-101)
3. 브랜치/PR에 이슈 키를 포함시켜 자동 연계합니다.
4. 진행 상태를 Linear 워크플로우 (Triage → Backlog → Todo → In Progress → In Review → Done)에 맞춰 업데이트합니다.

### 상태 범례

* **Triage**: 새로 생성된 스토리, 아직 분류 전
* **Backlog**: 우선순위 정리만 된 상태
* **Todo**: 바로 착수할 준비 완료
* **In Progress**: 개발/테스트 진행 중
* **In Review**: PR 리뷰/QA 중
* **Done**: 배포/문서 반영 완료

---

## Cycle 1 (2025-08-28 \~ 2025-09-04)

### Story: 사용자 로그인 (OAuth2)

* Epic: 사용자 인증/인가
* Issue: 로그인 기능 구현 (BRD-101)
* Task: [Task Tracker](task-tracker.md#issue-로그인-기능-구현-brd-101)
* Linear: BRD-101
* GitHub: `feature/login-oauth2`
* 상태: Todo

### Story: 사용자 회원가입

* Epic: 사용자 인증/인가
* Issue: 회원가입 기능 구현 (BRD-102)
* Task: [Task Tracker](task-tracker.md#issue-회원가입-기능-구현-brd-102)
* Linear: BRD-102
* GitHub: `feature/signup`
* 상태: Todo

---

## Cycle 2 (예정)

### Story: 워크스페이스 생성/삭제

* Epic: 워크스페이스 관리
* Issue: 워크스페이스 생성/삭제 (BRD-103)
* Linear: BRD-103
* GitHub: `feature/workspace-crud`
* 상태: Backlog

### Story: 워크스페이스 멤버 초대

* Epic: 워크스페이스 관리
* Issue: 워크스페이스 멤버 초대 (BRD-104)
* Linear: BRD-104
* GitHub: `feature/workspace-invite`
* 상태: Backlog

---

## Cycle 3 (예정)

### Story: 보드 생성/삭제

* Epic: 보드 관리
* Issue: 보드 생성/삭제 (BRD-105)
* Linear: BRD-105
* GitHub: `feature/board-crud`
* 상태: Backlog

### Story: 보드 멤버 관리

* Epic: 보드 관리
* Issue: 보드 멤버 관리 (BRD-106)
* Linear: BRD-106
* GitHub: `feature/board-member`
* 상태: Backlog

---

## Cycle 4 (예정)

### Story: 리스트 관리

* Epic: 카드/리스트 관리
* Issue: 리스트 관리 (BRD-107)
* Linear: BRD-107
* GitHub: `feature/list-crud`
* 상태: Backlog

### Story: 카드 관리

* Epic: 카드/리스트 관리
* Issue: 카드 관리 (BRD-108)
* Linear: BRD-108
* GitHub: `feature/card-crud`
* 상태: Backlog

---

## Cycle 5 (예정)

### Story: 다국어 지원 (i18n)

* Epic: i18n 및 UX 개선
* Issue: 다국어 지원 MVP (BRD-109)
* Linear: BRD-109
* GitHub: `feature/i18n`
* 상태: Backlog
