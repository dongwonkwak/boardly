# Task Tracker — Boardly (Issues × Tasks × Linear)

Epic → Issue → Task 구조 중, **Task 단위**의 진행 현황을 관리하는 아티팩트입니다. 각 Task는 Linear 이슈와 연결되며, 하루 이내 완료 가능한 최소 단위 작업을 목표로 합니다.

---

## 0) 사용법

1. Epic과 Issue가 정의되면, 각 Issue를 세부 Task로 분해합니다.
2. Task는 "개발자가 하루 이내에 끝낼 수 있는 크기"를 원칙으로 작성합니다.
3. 각 Task는 Linear에 등록하고, 이 문서에 링크합니다. (예: BRD-201)
4. Task 상태는 Linear 워크플로우 (Triage → Backlog → Todo → In Progress → In Review → Done)를 그대로 따릅니다.
5. Task 완료 기준은 Cycle DoD를 따릅니다.

### 상태 범례 (Linear 기준)

* **Triage**: 새로 생성된 Task, 아직 분류 전
* **Backlog**: 우선순위 정리만 된 Task
* **Todo**: 바로 착수할 준비 완료
* **In Progress**: 개발/테스트 진행 중
* **In Review**: PR 리뷰/QA 중
* **Done**: 배포/문서 반영 완료

---

## 1) Cycle 1 (2025-08-28 \~ 2025-09-04)

### Epic: 사용자 인증/인가

#### Issue: 로그인 기능 구현 (BRD-101)

* [ ] **백엔드: 로그인 API 엔드포인트 작성** (BRD-201)
* [ ] **백엔드: 로그인 API 단위 테스트 작성** (BRD-202)
* [ ] **프론트엔드: 로그인 폼 UI 작성** (BRD-203)
* [ ] **프론트엔드: OpenAPI Client 연결** (BRD-204)
* [ ] **프론트엔드: 로그인 성공/실패 플로우 처리** (BRD-205)

#### Issue: 회원가입 기능 구현 (BRD-102)

* [ ] **백엔드: 회원가입 API 엔드포인트 작성** (BRD-206)
* [ ] **백엔드: 회원가입 유효성 검증 추가** (BRD-207)
* [ ] **프론트엔드: 회원가입 폼 UI 작성** (BRD-208)
* [ ] **프론트엔드: 회원가입 API 연동** (BRD-209)
* [ ] **프론트엔드: 회원가입 완료 화면 처리** (BRD-210)

---

## 2) Definition of Done (DoD)

* [ ] 기능 구현 + 단위 테스트 통과
* [ ] OpenAPI 스펙 반영/갱신 완료
* [ ] i18n 적용 (필요한 경우)
* [ ] 문서화 (개발 문서, README, 변경 로그 등)
* [ ] 코드 리뷰 승인 및 머지

---

## 3) 참고

* 이 문서는 Epic/Issue 문서와 Cycle 문서와 함께 관리됩니다.
* Task 완료 여부는 Linear와 GitHub PR 상태를 기준으로 동기화합니다.
