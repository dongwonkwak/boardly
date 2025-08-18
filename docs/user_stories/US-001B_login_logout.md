---
id: US-001B
title: "로그인·로그아웃"
type: user_story
featureId: F-101
parentFeatureDoc: US-001_onboarding_account_management.md
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "온보딩·계정 관리"
tags: [MVP, Auth]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
  - ../errors.md
---

# 로그인·로그아웃 사용자 스토리 (US-001B)

## 사용자 스토리
- 나는 사용자로서, 이메일/비밀번호로 안전하게 로그인하고 내 리소스에 접근하고 싶다.
- 나는 사용자로서, 로그아웃하여 내 세션/토큰을 안전하게 종료하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 로그인
   - 입력: `email`, `password` 필수
   - 성공: 200과 인증 토큰(JWT access token, 필요 시 refresh token) 발급
   - 실패: 잘못된 자격 증명 → 401, 비활성/잠금 계정 → 403 또는 423(P1)
   - 보안: 비밀번호는 해시 비교(BCrypt); JWT에는 만료(`exp`) 포함
   - 레이트 리밋: 다수 실패 시 제한 적용(브루트 포스 방지)
2. 로그아웃
   - 클라이언트 저장 토큰 제거 또는 서버 측 블랙리스트/세션 무효화
   - 다중 기기 중 현재 기기만 로그아웃(P0), 전체 기기 로그아웃(P1)
   - 호출은 멱등적이며 토큰이 이미 무효여도 200 반환
3. 세션
   - 토큰 만료 시 보호 API는 401을 반환
   - 선택: "기억하기" 옵션으로 refresh 기반 재인증(P1)

## 비기능 (NFR)
- 성능: 로그인 응답 ≤ 600ms(평균)
- 보안: JWT 서명키 보호, HTTPS 필수, CSRF 대비(쿠키 사용 시 `SameSite=Lax`+CSRF 토큰)
- 감사(P1): 마지막 로그인 시각/아이피 기록

## 테스트 시나리오 (샘플)
- 성공 로그인 → 200 + 토큰, 보호 API 접근 가능
- 실패 로그인 → 401, 연속 실패 후 rate limit 동작
- 로그아웃 호출 후 보호 API 접근 → 401

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드
  - 400 InputError: 필수 입력 누락/형식 오류
  - 401 Unauthorized: 잘못된 자격 증명/만료 토큰
  - 403 PermissionDenied: 비활성/잠금 계정
  - 423 Locked: 잠금 계정(선택)
  - 429 TooManyRequests: 레이트 리밋
  - 500 InternalError
