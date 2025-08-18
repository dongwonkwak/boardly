---
id: US-001A
title: "회원가입"
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

# 회원가입 사용자 스토리 (US-001A)

## 사용자 스토리
- 나는 신규 사용자로서, 유효한 이메일과 강력한 비밀번호로 간편하게 가입해 Boardly를 사용하고 싶다.

## 수용 기준 (Acceptance Criteria)
1. 필수 입력값: `email`, `password`, `firstName`, `lastName`
2. 유효성 규칙
   - 이메일: 표준 형식이어야 함
   - 비밀번호: 최소 8자, 대문자 1+, 소문자 1+, 특수문자 1+ (SRS 3.1)
   - 이름: 1~50자, 한글/영문만 허용 (SRS 3.1)
3. 중복 방지: 이미 등록된 이메일이면 가입 실패(중복 불가)
4. 응답
   - 성공: 201/200, 생성된 사용자 식별자와 기본 설정 반환(정책에 따름)
   - 실패: 아래 "오류 응답(표준)" 참조
5. 보안/저장
   - 비밀번호는 BCrypt 단방향 해시로 저장(SRS 6.3)
   - Rate limiting 적용(브루트 포스 방지)

## 비기능 (NFR)
- API 응답 ≤ 800ms(평균)
- 가용성: 장애 발생 시에도 중복 가입 방지 일관성 보장(409)

## 테스트 시나리오 (샘플)
- 성공: 유효 입력 → 201/200, DB에 사용자 생성, 비밀번호 해시 저장
- 실패(400): 잘못된 이메일, 비밀번호 규칙 위반, 이름 길이 초과/미만
- 실패(409): 이미 존재하는 이메일
- 보안: 다건 시도 시 rate limit 동작 확인

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 400 InputError: 형식/데이터 오류(이메일/비밀번호/이름 규칙 위반) — `details` 포함
  - 409 ResourceConflict: `EMAIL_ALREADY_EXISTS`
  - 500 InternalError
- 공통 포맷: `code`, `message`, `timestamp`, `path`, `details?`, `context?`
