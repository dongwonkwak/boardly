# 📘 Boardly Documentation

Boardly 프로젝트의 전체 문서 구조와 목적을 정리한 가이드입니다. 모든 문서는 기획 → 설계 → 실행 → 운영까지 end-to-end 추적성을 제공하기 위해 구성되어 있습니다.

---

## 📂 폴더 구조

```
.
├── architecture/                    # 아키텍처 설계
│   ├── backend-architecture.md     # 백엔드 아키텍처
│   ├── frontend-architecture.md    # 프론트엔드 아키텍처
│   └── system-architecture.md      # 시스템 전체 아키텍처
├── design/                         # 설계 문서
│   ├── boardly-schema.md          # 데이터베이스 스키마
│   └── erd.md                     # ERD 다이어그램
├── management/                     # 프로젝트 관리
│   ├── development-workflow.md     # 개발 워크플로우
│   ├── issue-tracker.md           # 이슈 트래커
│   ├── linear-labels.md           # Linear 라벨 정의
│   ├── project-management-guide.md # 프로젝트 관리 가이드
│   └── task-tracker.md            # 작업 트래커
├── planning/                       # 계획 문서
│   ├── cycle-plan.md              # 사이클 계획
│   ├── epics-issues.md            # 에픽 및 이슈 정의
│   └── roadmap.md                 # 로드맵
└── principles/                     # 원칙 및 가이드라인
    ├── api-design-principles.md    # API 설계 원칙
    └── testing-strategy.md         # 테스트 전략
```

총 6개 디렉토리, 15개 파일

---

## 📑 디렉토리별 설명

### 1. `architecture/`

* **system-architecture.md**: 전체 시스템 구성 개요
* **backend-architecture.md**: 백엔드 구조 및 기술 스택
* **frontend-architecture.md**: 프론트엔드 구조 및 기술 스택

### 2. `design/`

* **boardly-schema.md**: 데이터베이스 스키마 정의 (Flyway 기반)
* **erd.md**: ERD 다이어그램 및 테이블 관계도

### 3. `management/`

* **development-workflow\.md**: Linear × GitHub × bugbot 연계 개발 워크플로우
* **issue-tracker.md**: 이슈/PR 진행 상태 추적 문서
* **linear-labels.md**: Linear 라벨 체계 정의
* **project-management-guide.md**: 전체 문서/프로세스 관리 가이드
* **task-tracker.md**: Issue를 Task 단위로 세분화한 실행 계획

### 4. `planning/`

* **roadmap.md**: 프로젝트 단계별 목표 정의 (Phase/MVP)
* **epics-issues.md**: Epic과 세부 Issue 정의
* **cycle-plan.md**: 사이클별 실행 계획 (Cycle 목표/DoD)

### 5. `principles/`

* **api-design-principles.md**: API 설계 원칙 및 규칙
* **testing-strategy.md**: 테스트 전략, 커버리지 목표 및 도구

---

## 🚀 활용 가이드

* **기획 단계**: `planning/roadmap.md` → `epics-issues.md` → `cycle-plan.md`
* **설계 단계**: `architecture/` + `design/`
* **실행 단계**: `management/task-tracker.md` + `development-workflow.md`
* **운영/추적 단계**: `issue-tracker.md` + `linear-labels.md` + `project-management-guide.md`
* **표준화/품질 보장**: `principles/`

---

## 📌 Notes

* 모든 문서는 최신 상태 유지 필요 (사이클 종료 시 검토)
* Linear/GitHub/문서 간 ID 및 링크 일관성 유지
* 포트폴리오 목적: 단순 코드가 아닌 **문서/기획/실행/운영 전체 관리 체계**를 보여줌
