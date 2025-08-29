# Linear Labels — Boardly

Boardly 프로젝트에서 사용할 Linear 라벨 체계를 정의한 문서입니다. Epic & Issues, Task Tracker, Cycle Plan과 연계해 이슈를 관리합니다.

---

## 🎯 Epic 라벨 (8개)

**도메인별 기능 그룹**

| 라벨명                      | 색상        | 설명           |
| ------------------------ | --------- | ------------ |
| `Epic: User Auth`        | 🔵 Blue   | 사용자 인증/인가 관련 |
| `Epic: Workspace`        | 🟢 Green  | 워크스페이스 관리    |
| `Epic: Board Management` | 🟣 Purple | 보드 관리        |
| `Epic: Card/List`        | 🟠 Orange | 카드/리스트 관리    |
| `Epic: i18n`             | 🟡 Yellow | 다국어 지원       |
| `Epic: Notification`     | 🔴 Red    | 알림 시스템       |
| `Epic: Real-time Collab` | ⚪ Gray    | 고급 실시간 협업    |
| `Epic: Operations`       | ⚫ Black   | 운영 및 상용화     |

---

## ⚡ Priority 라벨 (3개)

**개발 우선순위**

| 라벨명                | 색상        | 설명                   |
| ------------------ | --------- | -------------------- |
| `Priority: High`   | 🔴 Red    | 즉시 개발 필요 (MVP 1-2)   |
| `Priority: Medium` | 🟡 Yellow | 핵심 완성 후 개발 (MVP 3-4) |
| `Priority: Low`    | 🟢 Green  | 부가 가치 기능 (Phase 5+)  |

---

## 🛠️ Type 라벨 (4개)

**개발 범위**

| 라벨명                 | 색상        | 설명                 |
| ------------------- | --------- | ------------------ |
| `Type: Backend`     | 🔵 Blue   | 백엔드만 개발            |
| `Type: Frontend`    | 🟠 Orange | 프론트엔드만 개발          |
| `Type: Full Stack`  | 🟣 Purple | 백엔드+프론트엔드 (대부분)    |
| `Type: Integration` | ⚪ Gray    | API 연동, E2E 테스트 중심 |

---

## 📅 Phase 라벨 (5개)

**개발 단계**

| 라벨명              | 색상              | 설명               |
| ---------------- | --------------- | ---------------- |
| `Phase: MVP 1`   | 🟦 Light Blue   | 사용자 인증/인가        |
| `Phase: MVP 2`   | 🟩 Light Green  | 워크스페이스/보드 관리     |
| `Phase: MVP 3`   | 🟪 Light Purple | 카드/리스트 관리        |
| `Phase: MVP 4`   | 🟨 Light Yellow | i18n 확장 & 알림 시스템 |
| `Phase: Phase 5` | ⬜ Light Gray    | 운영 및 상용화         |

---

## ✨ Feature 라벨 (7개)

**기능적 특성**

| 라벨명                    | 색상        | 설명                            |
| ---------------------- | --------- | ----------------------------- |
| `Feature: i18n`        | 🟣 Purple | **모든 Issue 필수** - 국제화 작업      |
| `Feature: Real-time`   | 🟠 Orange | 실시간 협업, 동기화 기능 (WebSocket 포함) |
| `Feature: File Upload` | 🟢 Green  | 파일 업로드/다운로드                   |
| `Feature: Search`      | 🔵 Blue   | 검색/필터링                        |
| `Feature: Email`       | 🔴 Red    | 이메일 발송 (초대, 알림)               |
| `Feature: RBAC`        | ⚪ Gray    | 권한 관리 시스템                     |
| `Feature: Drag&Drop`   | 🟡 Yellow | 드래그앤드롭 기능                     |

> 개선: 기존 `Feature: WebSocket`을 `Feature: Real-time`에 통합.

---

## 🔧 Tech 라벨 (7개)

**기술적 복잡도**

| 라벨명                  | 색상        | 설명                |
| -------------------- | --------- | ----------------- |
| `Tech: API Design`   | 🔵 Blue   | 복잡한 API 설계 필요     |
| `Tech: Security`     | 🔴 Red    | 보안 관련 작업          |
| `Tech: Performance`  | 🟠 Orange | 성능 최적화 필요         |
| `Tech: Database`     | ⚪ Gray    | DB 스키마 변경/최적화     |
| `Tech: Architecture` | 🟣 Purple | 아키텍처 변경           |
| `Tech: DevOps`       | 🟢 Green  | 배포, CI/CD 관련      |
| `Tech: Testing`      | 🟡 Yellow | E2E/계약/복잡한 테스트 작업 |

> 개선: **Tech: Testing** 라벨을 추가하여 테스트 복잡도 추적 가능.

---

## 📌 라벨 운영 가이드라인

### 필수 라벨 (모든 Issue)

* **Epic**: 반드시 1개
* **Priority**: 반드시 1개
* **Type**: 반드시 1개
* **Phase**: 반드시 1개
* **Feature: i18n**: 반드시 포함

### 선택적 라벨 (상황에 따라)

* **추가 Feature 라벨**: 해당하는 기능이 있는 경우
* **Tech 라벨**: 기술적 복잡도가 있는 경우 (1-3개)

### 라벨 개수 권장사항

* **최소**: 5개 (Epic + Priority + Type + Phase + Feature\:i18n)
* **일반적**: 6-8개 (위 + 추가 Feature/Tech 라벨 1-3개)
* **최대**: 10개 (너무 많으면 관리 복잡)

---

## 🎯 라벨링 예시

### 간단한 Issue

```
Issue #101: 회원가입 기능 구현
✅ Epic: User Auth
✅ Priority: High  
✅ Type: Full Stack
✅ Phase: MVP 1
✅ Feature: i18n
✅ Tech: Security
```

### 복잡한 Issue

```
Issue #403: 카드 드래그&드롭 이동 및 실시간 동기화  
✅ Epic: Card/List
✅ Priority: High
✅ Type: Full Stack
✅ Phase: MVP 3
✅ Feature: i18n
✅ Feature: Real-time
✅ Feature: Drag&Drop
✅ Tech: API Design
✅ Tech: Performance
✅ Tech: Testing
```

### 운영 관련 Issue

```
Issue #803: 모니터링 및 운영 도구 통합
✅ Epic: Operations
✅ Priority: Low
✅ Type: Backend
✅ Phase: Phase 5
✅ Feature: i18n
✅ Tech: DevOps
✅ Tech: Performance
```

---

## 📊 실제 Linear 등록 순서

1. Epic 라벨 생성
2. Priority & Type 라벨 생성
3. Phase 라벨 생성
4. Feature 라벨 생성
5. Tech 라벨 생성

