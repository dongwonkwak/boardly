---
id: US-008
title: "활동 내역(보드)"
featureId: F-601
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "진행 추적"
tags: [MVP, Activity]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-008 활동 내역 (보드) (F-601, P0)

## 배경
보드 내 주요 활동(카드 생성/이동/수정, 댓글 추가 등)을 시간순으로 기록·표시하여 투명성을 높인다.

참조: `PRD.md` F-601, `SRS.md` 3.8, `story_map.md` 8번 백본

## 사용자 스토리
- 나는 사용자로서, 보드에서 누가 언제 무엇을 했는지 활동 타임라인으로 확인하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 기록 대상
   - 카드 생성/이동/수정, 댓글 추가/수정/삭제, 라벨 변경, 마감일 변경
2. 표시
   - 활동 유형, 실행자, 관련 리소스(카드/리스트/보드), 타임스탬프가 표시된다.
3. 정렬/필터(선택)
   - 최신순 정렬, 유형/사용자별 필터 제공(선택)

## 비기능 (NFR)
- 조회 ≤ 500ms(최근 N건), 페이지네이션 지원
- 보안: JWT 인증, 보드 접근 권한 검증

## 테스트 시나리오 (샘플)
- 카드 이동 → 활동 로그에 이동 기록 생성
- 댓글 삭제 → 활동 로그에 반영
- 페이지네이션으로 과거 기록 조회

## 범위
- 포함(P0): 보드 단위 활동 기록/조회
- 제외(P1): 워크스페이스 통합 피드, 알림 푸시

## 추적
- Feature: F-601
- Links: `SRS.md#38-활동-내역`

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 보드 활동 열람 권한 없음
  - 404 NotFound: 보드 미발견
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`