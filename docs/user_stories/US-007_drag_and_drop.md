---
id: US-007
title: "드래그 앤 드롭"
featureId: F-401
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "정리·우선순위화"
tags: [MVP, DnD]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-007 드래그 앤 드롭 (F-401, P0)

## 배경
리스트와 카드는 드래그 앤 드롭으로 재배치되며, 이는 보드의 우선순위화 경험을 좌우한다. 이미 각 도메인 스토리(리스트/카드)에 기본 정렬 기준이 있으나, 본 스토리는 공통 DnD UX와 일관성/영속성 요구를 명확히 한다.

참조: `PRD.md` F-401, `SRS.md` 3.6/3.7, `story_map.md` 7번 백본

## 사용자 스토리
- (리스트 재배치) 나는 사용자로서, 보드 상단에서 리스트 순서를 마음대로 바꾸고 싶다.
- (카드 재배치) 나는 사용자로서, 리스트 내/리스트 간 카드 순서를 바꾸고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 상호작용
   - 드래그 시작/드랍 지점에 시 بص각적 가이드가 표시된다.
   - 모바일/데스크톱 모두 동작한다.
2. 영속성
   - 드랍 완료 시 서버에 `position`이 저장되고 새로고침 후에도 순서가 유지된다.
3. 일관성
   - 리스트 이동 시 해당 리스트 내 카드의 `position`은 영향을 받지 않는다.
   - 카드 이동 시 원본/목적지 리스트 양쪽의 `position`이 재계산되어 연속성이 유지된다.
4. 접근성(권장)
   - 키보드 이동(상/하/좌/우) 또는 대체 조작을 제공한다.

## 비기능 (NFR)
- 반응 ≤ 0.5초, 프레임 드랍 최소화
- 대량 요소(리스트≤20, 카드≤100/리스트)에서도 부드럽게 동작

## 테스트 시나리오 (샘플)
- 리스트 DnD → 서버 저장/재로딩 유지
- 카드 DnD(동일 리스트) → position 재배치
- 카드 DnD(다른 리스트) → listId 변경 및 연속성 검증

## 범위
- 포함: DnD 공통 UX/영속성
- 제외: 리스트/카드 CRUD 자체(각 스토리에서 다룸)

## 추적
- Feature: F-401
- Links: `SRS.md#36-리스트`, `SRS.md#37-카드-및-카드-상세-기능`

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 대상 리소스 이동 권한 없음
  - 404 NotFound: 리스트/카드 미발견
  - 409 ResourceConflict: 동시 DnD 충돌
  - 422 BusinessRuleViolation: 리스트/카드 수 제한으로 인한 이동 불가
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`