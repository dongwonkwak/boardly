---
id: US-015
title: "검색 및 필터링"
featureId: F-704
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "가치 탐색"
tags: [Search, Filter]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-015 검색 및 필터링 (F-704, P1)

## 배경
많은 카드 중 필요한 정보를 빠르게 찾기 위해 키워드/담당자/라벨/마감일 기준의 검색·필터가 필요하다.

참조: `PRD.md` F-704

## 사용자 스토리
- 나는 사용자로서, 보드에서 조건에 맞는 카드만 빠르게 찾고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 조건
   - 키워드, 라벨, 마감일(범위), 완료 여부로 필터.
2. 결과
   - 필터 적용 시 목록/보드 뷰에 즉시 반영.
3. 상태 저장(선택)
   - URL 쿼리/로컬 상태로 필터 유지.

## 비기능 (NFR)
- 대량 데이터에서도 500ms 내 결과

## 테스트 시나리오 (샘플)
- 라벨=버그 + 미완료 + 마감<오늘 → 결과 일치

## 범위
- 포함(P1): 기본 검색/필터
- 제외: 고급 쿼리, 전체 워크스페이스 전역 검색(확장)

## 추적
- Feature: F-704

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 400 InputError: 잘못된 필터 파라미터
  - 403 PermissionDenied: 보드 접근 권한 없음
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `details`