---
id: US-009
title: "다국어(i18n)"
featureId: F-701
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "다국어 환경"
tags: [MVP, i18n]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-009 다국어(i18n) (F-701, P0)

## 배경
한국어/영어를 지원하고, 사용자가 선호 언어를 선택·전환할 수 있어야 한다.

참조: `PRD.md` F-701, `SRS.md` 6.5, `story_map.md` 9번 백본

## 사용자 스토리
- (감지) 나는 사용자로서, 처음 접속 시 브라우저 언어가 자동으로 반영되길 원한다.
- (전환) 나는 사용자로서, UI에서 언어를 바꾸면 전체 화면이 즉시 전환되길 원한다.

## 수용 기준 (Acceptance Criteria)
1. 초기 언어
   - Accept-Language를 기본값으로 설정하되 사용자 설정이 있으면 이를 우선한다.
2. 전환
   - 언어 선택 시 전체 UI 텍스트가 즉시 전환된다(핫 스왑).
3. 리소스 관리
   - 텍스트는 `ko.json`/`en.json` 등 리소스 파일로 분리 관리한다.

## 비기능 (NFR)
- 전환 반응 ≤ 300ms
- 백엔드/프론트 메시지 일관성(`messages.properties` 계열)

## 테스트 시나리오 (샘플)
- 한국어 브라우저 → 기본 한국어 렌더링
- 설정에서 영어 선택 → 즉시 영어로 전환, 재로딩 후에도 유지

## 범위
- 포함: 한국어/영어, 런타임 전환
- 제외: 추가 언어(P1)

## 추적
- Feature: F-701
- Links: `SRS.md#65-다국어-지원`

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 400 InputError: 잘못된 언어 코드 입력
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `details`