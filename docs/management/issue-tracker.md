# Issue Tracker — Boardly (Stories × Linear × GitHub)

기능별 진행 현황을 한눈에 관리하는 문서입니다. 사용자 스토리 문서(캔버스)와 Linear 이슈, PR을 서로 링크합니다. Epic & Issues, Task Tracker, Cycle Plan과는 달리 **상태 추적**에 초점이 맞춰져 있습니다.

---

## 0) 사용법

1. 새로운 기능을 시작할 때 사용자 스토리 문서를 먼저 만들고, 아래 표에 링크합니다.
2. 스토리에서 파생된 Linear 이슈를 등록해 링크합니다. (예: BRD-123)
3. 브랜치/PR에 이슈 키를 포함하여 자동으로 연계합니다.
4. 진행 상태를 Triage → Backlog → Todo → In Progress → In Review → Done 흐름에 맞춰 업데이트합니다.

### 상태 범례 (Linear 워크플로우 기준)

* **Triage**: 새로 생성된 이슈, 아직 분류 전
* **Backlog**: 우선순위 정리만 된 상태
* **Todo**: 바로 착수할 준비 완료
* **In Progress**: 개발/테스트 진행 중
* **In Review**: PR 리뷰/QA 중
* **Done**: 배포/문서 반영 완료
* **Canceled/Duplicate**: 사용하지 않음(특수 상황만)

---

## 1) 사이클별 진행 현황

| Cycle   | Epic      | Issue            | Linear Key | PR 링크       | 상태          |
| ------- | --------- | ---------------- | ---------- | ----------- | ----------- |
| Cycle 1 | 사용자 인증/인가 | 회원가입 기능 구현       | BRD-101    | - | 📝 Backlog |
| Cycle 1 | 사용자 인증/인가 | 로그인 기능 구현        | BRD-102    | - | 📝 Backlog |
| Cycle 2 | 사용자 인증/인가 | Refresh Token 갱신 | BRD-103    | - | 📝 Backlog |

---

## 2) 관리 원칙

* Issue Tracker는 **상태만** 기록하며, 세부 Task는 Task Tracker 참조
* Epic & Issues 문서의 Issue ID ↔ Linear Key를 항상 매핑
* GitHub PR은 Linear Key를 브랜치/커밋 메시지에 포함해 자동 연결
* 문서 내 상태는 최소 주 2회 업데이트

---

## Notes

* Issue Tracker는 Roadmap/Epic & Issues/Task Tracker/Cycle Plan 문서와 병렬적으로 운영됨
* 불필요한 라벨 표기를 제거하고, 진행 현황 중심으로 단순화
* Linear와 GitHub를 연결하는 **운영 중심 문서**로만 활용
