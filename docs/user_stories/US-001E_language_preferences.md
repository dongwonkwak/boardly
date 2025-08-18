---
id: US-001E
title: "언어 설정(i18n)"
type: user_story
featureId: F-101
parentFeatureDoc: US-001_onboarding_account_management.md
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "온보딩·계정 관리"
tags: [MVP, i18n]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
  - ../errors.md
---

# 언어 설정(i18n) 사용자 스토리 (US-001E)

## 사용자 스토리
- 나는 사용자로서, 한국어/영어 중 선호 언어를 설정하고 즉시 UI가 해당 언어로 반영되길 원한다.

## 수용 기준 (Acceptance Criteria)
1. 기본값
   - 최초 접속 시 브라우저 언어(`ko`/`en`)를 기본값으로 설정
   - 미지원 언어의 경우 `en`으로 폴백
2. 변경
   - 사용자는 `language`를 `ko` 또는 `en`으로 설정/저장할 수 있다.
   - 저장 후 즉시 전체 UI 텍스트가 선택 언어로 재렌더링된다(새로고침 불필요).
   - 설정은 프로필에 영속 저장되어 디바이스 간 일관되게 적용된다.
3. 서버/클라이언트
   - 프론트: i18next 기반 리소스 로딩, Lazy loading 권장
   - 백엔드: `MessageSource` 기반 로케일 응답(검증 메시지 등)

## 비기능 (NFR)
- 성능: 언어 전환 시 텍스트 스위칭 ≤ 300ms, 번들 지연 로딩 포함 ≤ 800ms
- 사용성: 전환 컨트롤은 글로벌 내비게이션/프로필 메뉴에 위치

## 테스트 시나리오 (샘플)
- 브라우저 언어가 `ko` → 첫 진입 시 한국어 표시
- 언어를 `en`으로 변경 → 즉시 모든 텍스트가 영어로 변경
- 다른 기기에서 로그인 → 저장된 언어가 적용됨

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드
  - 400 InputError: 지원하지 않는 언어 코드
  - 401 Unauthorized: 인증 누락
  - 500 InternalError
