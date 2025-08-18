---
id: US-001C
title: "프로필 관리"
type: user_story
featureId: F-101
parentFeatureDoc: US-001_onboarding_account_management.md
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "온보딩·계정 관리"
tags: [MVP, Account]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
  - ../errors.md
---

# 프로필 관리 사용자 스토리 (US-001C)

## 사용자 스토리
- 나는 사용자로서, 내 이름과 선호 언어 등 프로필 정보를 조회·수정하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 조회
   - 인증된 사용자는 자신의 프로필을 조회할 수 있다.
   - 반환 필드: `id`, `email`, `firstName`, `lastName`, `language`, `createdAt`, `updatedAt`
2. 수정
   - 수정 가능 필드: `firstName`, `lastName`, `language`
   - 유효성: 이름 1~50자, 한글/영문만 허용(SRS 3.1); `language` ∈ {`ko`, `en`}
   - 성공 시 200과 갱신된 리소스 반환, `updatedAt` 갱신
   - 동시성(P1): 조건부 갱신(ETag/If-Match)로 경합 시 409
3. 제약
   - 이메일 변경은 범위 외(P1)

## 비기능 (NFR)
- 성능: 조회 ≤ 300ms, 수정 ≤ 600ms(평균)
- 보안: JWT 인증 필수; 본인 소유 데이터만 접근 가능
- 사용성: 필드별 오류 메시지 및 포커스 이동 제공

## 테스트 시나리오 (샘플)
- 프로필 조회 성공 → 200 + 필수 필드 포함
- 프로필 수정 성공 → 200 + 값 반영, `updatedAt` 변경
- 유효성 실패 → 400 + 필드별 메시지
- 동시 수정 경합(P1) → 409

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드
  - 400 InputError: 형식/데이터 오류(이름/언어 규칙 위반)
  - 401 Unauthorized: 인증 누락/실패
  - 403 PermissionDenied: 타 사용자 프로필 접근 시도
  - 409 ResourceConflict: 동시성 충돌(P1)
  - 500 InternalError
