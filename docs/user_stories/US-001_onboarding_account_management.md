---
id: US-001
title: "온보딩·계정 관리"
featureId: F-101
priority: P0
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "온보딩·계정 관리"
tags: [MVP, Auth, i18n]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-001 온보딩·계정 관리 (F-101, P0)

## 배경
Boardly의 첫 경험은 계정 생성과 안전한 접근에서 시작된다. 사용자는 이메일/비밀번호로 가입·로그인하고, 프로필 및 비밀번호를 관리하며, 선호 언어를 설정할 수 있어야 한다.

참조: `PRD.md` F-101, `SRS.md` 3.1, 6.5, `story_map.md` 1번 백본

## 사용자 스토리
- (가입) 회원가입 — `US-001A_signup.md`
- (로그인/로그아웃) 로그인·로그아웃 — `US-001B_login_logout.md`
- (프로필 관리) 프로필 관리 — `US-001C_profile_management.md`
- (비밀번호 변경) 비밀번호 변경 — `US-001D_password_change.md`
- (언어 설정) 언어 설정(i18n) — `US-001E_language_preferences.md`

## 수용 기준 (Acceptance Criteria)
각 하위 사용자 스토리 문서를 참조하세요.
 - 회원가입: `US-001A_signup.md`
 - 로그인·로그아웃: `US-001B_login_logout.md`
 - 프로필 관리: `US-001C_profile_management.md`
 - 비밀번호 변경: `US-001D_password_change.md`
 - 언어 설정(i18n): `US-001E_language_preferences.md`

## 비기능 (NFR)
- 초기 페이지 로딩 ≤ 2초, 상호작용 반응 ≤ 0.5초
- 보안: BCrypt 해시 저장, JWT 인증, CSRF/XSS/SQLi 대응, Rate Limiting
- i18n: i18next(프론트), MessageSource(백엔드) 기반, 동적 전환 지원

## 테스트 시나리오 (샘플)
- 성공 가입: 유효 입력 → 201/200 응답, DB에 사용자 생성됨
- 실패 가입: 약한 비밀번호 → 400과 규칙 위반 메시지
- 로그인 성공 → JWT 저장, 보호 API 200
- 로그인 실패 → 401
- 프로필 수정 성공/실패(유효성)
- 비밀번호 변경 성공/실패(현재 비밀번호 불일치, 규칙 미준수)
- 언어 전환 → 텍스트 리소스가 선택 언어로 즉시 변경

## 범위
- 포함: 회원가입, 로그인/로그아웃, 프로필 조회·수정, 비밀번호 변경, 언어 설정
- 제외: 소셜 로그인(Px), 이메일 인증(Px), 2FA(Px)

## 추적
- Feature: F-101
- Epic/Backbone: 온보딩·계정 관리
- Links: `PRD.md#4-핵심-기능-요구사항`, `SRS.md#31-사용자-인증`, `SRS.md#65-다국어-지원`

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 400 InputError: 입력 형식/데이터 오류(잘못된 이메일, 비밀번호 규칙 위반)
  - 409 ResourceConflict: 이메일 중복 가입
  - 500 InternalError: 내부 서버 오류
- 응답 포맷: `code`, `message`, `timestamp`, `path`, 선택적으로 `details`(400), `context`