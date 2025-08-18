---
id: US-014
title: "첨부파일"
featureId: F-703
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "카드 상세 확장"
tags: [Card, Attachment]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-014 첨부파일 (F-703, P1)

## 배경
업무 관련 파일을 카드에 첨부하여 맥락을 보존한다.

참조: `PRD.md` F-703, `SRS.md` 5.1(추가 엔티티)

## 사용자 스토리
- 나는 사용자로서, 로컬/클라우드 파일을 카드에 첨부/다운로드/삭제하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 업로드
   - 파일 크기 ≤ 10MB, 타입/보안 검사.
2. 목록/다운로드/삭제
   - 첨부 목록 제공, 개별 다운로드/삭제 가능.
3. 메타데이터
   - 파일명/크기/업로더/업로드 시각 저장.

## 비기능 (NFR)
- 스토리지 안전성, 바이러스 스캔(선택)

## 테스트 시나리오 (샘플)
- 11MB 업로드 → 거부
- 삭제 후 목록에서 제거 확인

## 범위
- 포함(P1): 업로드/다운로드/삭제
- 제외: 미리보기 변환(확장)

## 추적
- Feature: F-703

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 400 InputError: 파일 크기/타입 유효성 실패
  - 403 PermissionDenied: 첨부 수정 권한 없음
  - 404 NotFound: 카드/첨부 미발견
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `details`, `context`