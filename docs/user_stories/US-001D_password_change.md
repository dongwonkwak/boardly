---
id: US-001D
title: "비밀번호 변경"
type: user_story
featureId: F-101
parentFeatureDoc: US-001_onboarding_account_management.md
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "온보딩·계정 관리"
tags: [MVP, Auth, Security]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
  - ../errors.md
---

# 비밀번호 변경 사용자 스토리 (US-001D)

## 사용자 스토리
- 나는 사용자로서, 현재 비밀번호를 확인한 뒤 정책에 맞는 새 비밀번호로 안전하게 변경하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 입력
   - 필수: `currentPassword`, `newPassword`, `confirmNewPassword`
2. 검증
   - `currentPassword`가 올바르지 않으면 변경 불가(401 또는 400)
   - `newPassword` 정책: 최소 8자, 대문자 1+, 소문자 1+, 특수문자 1+ (SRS 3.1)
   - `newPassword` = `confirmNewPassword`
   - 최근 비밀번호 재사용 금지(P1)
3. 처리
   - 성공 시 200, BCrypt 해시로 저장, `passwordChangedAt` 갱신
   - 보안: 변경 직후 refresh 토큰 전체 무효화(P0), 모든 기기 로그아웃(P1)
   - 감시: 과도한 실패 시 rate limit 적용

## 비기능 (NFR)
- 성능: 평균 응답 ≤ 600ms
- 보안: 평문 비밀번호 저장 금지/로그 금지, 전달 채널은 HTTPS

## 테스트 시나리오 (샘플)
- 성공: 올바른 현재 비밀번호 + 정책 충족 새 비밀번호 → 200, 이후 이전 토큰으로 보호 API 접근 시 401
- 실패: 현재 비밀번호 불일치 → 401/400
- 실패: 정책 미충족 → 400/422 + 규칙별 메시지

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드
  - 400 InputError: 형식/데이터 오류(정책 미충족 등)
  - 401 Unauthorized: 현재 비밀번호 불일치 또는 인증 누락
  - 409 ResourceConflict: 동시 변경 충돌
  - 422 BusinessRuleViolation: 정책 위반 상세
  - 500 InternalError
