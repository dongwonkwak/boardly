---
id: US-010
title: "협업: 초대·역할·권한(RBAC)"
featureId: F-402
priority: P1
status: Todo
storyPoints: TBD
assignee: TBD
reporter: AI-GPT
epic: "접근제어·초대"
tags: [Collaboration, RBAC]
relatedDocs:
  - ../PRD.md
  - ../SRS.md
  - ../story_map.md
---

# US-010 협업: 초대·역할·권한(RBAC) (F-402, P1)

## 배경
팀 협업을 위해 보드/워크스페이스 단위로 멤버를 초대하고 역할(OWNER/EDITOR/VIEWER 등)에 따른 권한을 부여한다.

참조: `PRD.md` F-402, `SRS.md` 3.3~3.4, `workspace-board-permissions.md`, 권한 매트릭스/우선순위, 초대 프로세스

## 사용자 스토리
- (초대) 나는 워크스페이스 OWNER/ADMIN 또는 보드 BOARD_ADMIN로서, 이메일 또는 링크로 멤버를 초대하고 싶다.
- (역할) 나는 워크스페이스 OWNER/ADMIN 또는 보드 BOARD_ADMIN로서, 멤버의 역할을 설정/변경하고 싶다.
- (접근 제어) 나는 사용자로서, 내 역할에 맞는 범위에서만 리소스에 접근하고 싶다.
- (권한 우선순위) 나는 워크스페이스 OWNER/ADMIN로서, 모든 보드에 대해 모든 권한을 가져야 한다.

## 수용 기준 (Acceptance Criteria)
1. 초대
   - 초대 방식: 이메일/링크, 상태: PENDING/ACCEPTED/DECLINED/EXPIRED(7일).
   - 워크스페이스 초대: OWNER/ADMIN만 가능, 기본 역할 MEMBER 부여.
   - 보드 초대: BOARD_ADMIN만 가능, 기본 역할 BOARD_VIEWER 부여.
2. 역할/권한
   - 워크스페이스 역할: OWNER, ADMIN, MEMBER
   - 보드 역할: BOARD_ADMIN, BOARD_EDITOR, BOARD_VIEWER
   - 권한 매트릭스에 따라 API 접근 제어(`workspace:*`, `board:*`, `content:*` 등).
   - 워크스페이스 권한 > 보드 권한 우선순위 적용.
3. 접근 제어
   - JWT 인증 후, 워크스페이스 멤버십 → 워크스페이스 역할 → 보드 멤버십 → 보드 역할 순서로 권한 검증.
   - 권한 충돌 시 워크스페이스 권한이 우선 적용.

## 비기능 (NFR)
- 보안: 최소 권한 원칙, 멀티테넌시 데이터 격리, 감사 로그
- 사용성: 초대 만료 안내, 역할 변경 시 즉시 반영, 권한 충돌 로깅

## 테스트 시나리오 (샘플)
- 워크스페이스 OWNER가 멤버 초대 → 수락 후 MEMBER 역할 부여
- 보드 BOARD_ADMIN이 게스트 초대 → 수락 후 BOARD_VIEWER 역할 부여
- BOARD_VIEWER가 카드 수정 시도 → 403
- 워크스페이스 OWNER가 보드 BOARD_ADMIN의 보드 삭제 → 성공 (워크스페이스 권한 우선)
- 초대 만료 후 수락 시도 → 410/400

## 범위
- 포함(P1): 초대, 역할 할당/변경, 권한 검증
- 제외: 실시간 협업 상태 표시(별도), 개인 알림 센터

## 추적
- Feature: F-402, SRS 3.3, 3.4, workspace-board-permissions.md

## 오류 응답(표준)
- 참고: `../errors.md`
- 상태 코드 적용
  - 403 PermissionDenied: 초대/역할 변경 권한 없음
  - 404 NotFound: 사용자/보드/워크스페이스/초대 토큰 미발견
  - 409 ResourceConflict: 이미 초대됨/역할 충돌
  - 412 PreconditionFailed: 워크스페이스 역할 고정 원칙 위배 등 전제 조건 실패
  - 422 BusinessRuleViolation: 권한 매트릭스 위반 요청
  - 500 InternalError
- 응답 포맷: `code`, `message`, `timestamp`, `path`, `context`