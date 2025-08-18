---
id: US-016
title: "담당자 할당"
featureId: F-301
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "카드 상세 확장"
tags: [Card, Assignee]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-016 담당자 할당 (Assignees) (F-301 확장, P1)

## 배경
카드에 책임자를 명시하여 소유권과 책임을 분명히 한다. 협업 기능과 역할/권한 체계와 연동된다.

참조: `PRD.md` F-301(세부), `SRS.md` 3.7

## 사용자 스토리
- 나는 PM으로서, 카드에 한 명 이상 담당자를 지정/해제하고 싶다.
- 나는 보드 멤버로서, 내가 담당자인 카드를 쉽게 찾고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 지정/해제
   - 보드에 접근 권한이 있는 사용자 중에서 선택하여 담당자 추가/제거.
   - 워크스페이스 멤버, 보드 멤버, BOARD_ONLY 사용자 모두 담당자로 지정 가능.
2. 표시/필터
   - 카드 UI에 담당자 아바타 표시, 담당자 기준 필터 가능.
3. 권한
   - 담당자만 수정 가능 같은 추가 제약은 정책에 따라 선택 적용.

## 비기능 (NFR)
- 반응 ≤ 0.5초, 멤버 100명 규모에서도 사용성 유지

## 테스트 시나리오 (샘플)
- 담당자 추가/제거 → 카드에 즉시 반영, 필터 동작

## 범위
- 포함(P1): 담당자 다중 지정/해제, 표시, 필터
- 제외: 업무량 균형/알림 연계(별도)

## 추적
- Feature: F-301 확장

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 담당자 변경 권한 없음
  - 404 NotFound: 카드/사용자 미발견
  - 409 ResourceConflict: 중복 지정
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`