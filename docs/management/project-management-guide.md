# Project Management Guide — Boardly

Boardly 프로젝트의 문서, 라벨, 워크플로우를 통합적으로 관리하기 위한 가이드입니다. Linear, GitHub, Cursor bugbot과 연계하여 **기획 → 실행 → 추적 → 배포**까지 end-to-end 추적성을 확보합니다.

---

## 📂 문서 체계

| 문서                   | 역할                          | 위치                                        |
| -------------------- | --------------------------- | ----------------------------------------- |
| Roadmap              | 프로젝트 단계별 목표 정의              | `docs/planning/roadmap.md`                |
| Epic & Issues        | Epic 선정 및 세부 Issue 정의       | `docs/planning/epic-issues.md`            |
| Task Tracker         | Issue를 Task 단위로 세분화         | `docs/management/task-tracker.md`         |
| Cycle Plan           | Task를 Cycle 단위로 운영 계획화      | `docs/planning/development-cycle-plan.md` |
| Issue Tracker        | Issue/PR 진행 상태 추적           | `docs/management/issue-tracker.md`        |
| Linear Labels        | Linear 라벨 체계 정의             | `docs/management/linear-labels.md`        |
| Development Workflow | 개발 워크플로우 정의 (PR, bugbot 포함) | `docs/management/development-workflow.md` |

---

## 🎯 Linear 라벨 운영

* **Epic 라벨**: 기능 그룹 (Auth, Workspace, Board, Card/List 등)
* **Priority 라벨**: High/Medium/Low 우선순위
* **Type 라벨**: Backend/Frontend/Full Stack/Integration
* **Phase 라벨**: MVP1\~4, Phase 5
* **Feature 라벨**: 기능 속성 (i18n, Real-time, File Upload 등)
* **Tech 라벨**: 기술적 복잡도 (Security, Performance, Testing 등)

📌 규칙:

* 모든 Issue에는 최소 5개 라벨 (Epic, Priority, Type, Phase, Feature\:i18n)
* 상황에 따라 Feature/Tech 라벨을 추가해 6\~8개 수준 유지

---

## ⚡ 개발 워크플로우 (Linear × GitHub × bugbot)

1. **Issue 정의**: Epic & Issues → Task Tracker 기반으로 Linear Issue 생성
2. **브랜치 생성**: `feat/{domain}-{feature}` 네이밍 규칙 적용
3. **커밋 메시지**: `[BRD-XXX] 기능명` (Linear Key 포함)
4. **PR 생성**: 제목 `[BRD-XXX] 기능명`, 본문에 구현 내용/테스트/문서 링크
5. **bugbot 자동 리뷰**: 코드 분석, 테스트 제안, 버그 지적 자동화
6. **리뷰 반영**: 추가 커밋, 상태 `In Progress` → `In Review`
7. **머지 & 완료**: Linear 상태 `Done`, Issue Tracker 문서 업데이트

---

## 📊 추적성 관리

* **Epic & Issues → Task Tracker → Cycle Plan**: 계획/실행
* **Issue Tracker**: 상태 추적 (Linear ↔ GitHub 연결)
* **Development Workflow**: 실행 표준화
* **Linear Labels**: 필터/우선순위/속성 관리

---

## ✅ 운영 가이드라인

* 문서는 항상 최신 상태 유지 (Cycle 종료 시 일괄 리뷰)
* Linear ↔ GitHub ↔ 문서 간 ID/링크 일관성 필수
* bugbot 리뷰는 자동화된 지원 도구, 최종 책임은 개발자에게 있음
* 포트폴리오 관점에서 **문서/이슈/PR/코드**가 완전하게 연결된 모습을 제공

---

## Notes

이 가이드는 Boardly 프로젝트의 **엔드투엔드 관리 체계**를 정의합니다. 모든 문서는 `docs/` 폴더 내에서 관리되며, 관리 문서는 `docs/management/` 하위, 기획 문서는 `docs/planning/` 하위에 둡니다.
