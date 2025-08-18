---
id: US-013
title: "체크리스트"
featureId: F-702
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "카드 상세 확장"
tags: [Card, Checklist]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-013 체크리스트 (F-702, P1)

## 배경
복잡한 작업을 작은 단위로 쪼개 진행률을 관리한다.

참조: `PRD.md` F-702, `SRS.md` 5.1(추가 엔티티), `story_map.md` 6번 백본(P1)

## 사용자 스토리
- 나는 사용자로서, 카드 내부에 체크리스트와 항목을 추가/완료하여 진행률을 확인하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 체크리스트
   - 생성/이름 변경/삭제, `position`으로 정렬.
2. 항목
   - 생성/수정/삭제/완료 토글, `position` 정렬.
3. 진행률
   - 완료 비율(%)을 시 بص각적으로 표시.

## 비기능 (NFR)
- 반응 ≤ 0.5초, 대량 항목에서도 스크롤/가상화 고려

## 테스트 시나리오 (샘플)
- 항목 완료 토글 → 진행률 계산 업데이트

## 범위
- 포함(P1): 체크리스트/항목 CRUD, 완료율
- 제외: 템플릿(확장)

## 추적
- Feature: F-702

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 체크리스트/항목 변경 권한 없음
  - 404 NotFound: 카드/체크리스트/항목 미발견
  - 409 ResourceConflict: 동시 편집 충돌
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`