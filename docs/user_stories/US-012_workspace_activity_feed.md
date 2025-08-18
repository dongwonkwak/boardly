---
id: US-012
title: "워크스페이스 활동 통합"
featureId: F-602
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "진행 추적"
tags: [Collaboration, Activity]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-012 워크스페이스 활동 통합 (F-602, P1)

## 배경
여러 보드를 사용하는 팀은 워크스페이스 수준에서 전체 활동을 한 화면으로 조망할 필요가 있다.

참조: `PRD.md` F-602, `story_map.md` 8번 백본(P1)

## 사용자 스토리
- 나는 팀 리더로서, 워크스페이스의 모든 보드 활동을 통합 피드로 보고 싶다.
- 나는 워크스페이스 OWNER/ADMIN로서, 권한 관련 활동(초대, 역할 변경, 권한 충돌)을 추적하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 범위
   - 워크스페이스 내 보드들의 활동을 시간순으로 통합하여 제공.
   - 권한 관련 활동: 초대, 역할 변경, 권한 충돌 등 포함.
2. 필터/정렬
   - 보드/사용자/유형별 필터, 최신순 정렬.
   - 권한 관련 활동 별도 필터링 옵션.
3. 권한
   - 사용자는 자신이 접근 가능한 보드의 활동만 볼 수 있다.
   - 워크스페이스 OWNER/ADMIN은 모든 권한 관련 활동을 볼 수 있다.

## 비기능 (NFR)
- 조회 ≤ 800ms(페이지네이션), 인덱스 최적화

## 테스트 시나리오 (샘플)
- 특정 보드 활동만 필터링, 사용자별 필터 조합 동작
- 권한 관련 활동 필터링, OWNER/ADMIN 권한별 활동 조회 차이

## 범위
- 포함(P1): 통합 피드 조회
- 제외: 푸시 알림 연계(별도)

## 추적
- Feature: F-602

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 워크스페이스 활동 열람 권한 없음
  - 404 NotFound: 워크스페이스/보드 미발견
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`