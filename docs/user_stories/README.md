# User Stories (GPT Generated)

Boardly의 사용자 스토리를 `story_map.md` 기반으로 정리했습니다. P0는 MVP, P1은 차기 릴리즈 범위를 의미합니다.

## Index

### P0 (MVP)
1. [US-001 온보딩·계정 관리 (F-101)](US-001_onboarding_account_management.md)
2. [US-002 워크스페이스 구성 (F-150)](US-002_workspace_management.md)
3. [US-003 보드 생성·설정 (F-201)](US-003_board_creation_and_settings.md)
4. [US-004 리스트 워크플로 (F-202)](US-004_list_workflow.md)
5. [US-005 카드 관리 (F-203)](US-005_card_management.md)
6. [US-006 카드 상세: 설명/댓글/라벨/마감일 (F-301)](US-006_card_detail_core.md)
7. [US-007 드래그 앤 드롭 (F-401)](US-007_drag_and_drop.md)
8. [US-008 활동 내역(보드) (F-601)](US-008_activity_feed_board.md)
9. [US-009 다국어(i18n) (F-701)](US-009_internationalization.md)

### P1 (Next)
10. [US-010 협업: 초대·역할·권한(RBAC) (F-402)](US-010_collaboration_invites_and_rbac.md)
11. [US-011 알림: 인앱·개인 알림 센터 (F-501, F-603)](US-011_notifications_in_app_and_center.md)
12. [US-012 워크스페이스 활동 통합 (F-602)](US-012_workspace_activity_feed.md)
13. [US-013 체크리스트 (F-702)](US-013_checklists.md)
14. [US-014 첨부파일 (F-703)](US-014_attachments.md)
15. [US-015 검색 및 필터링 (F-704)](US-015_search_and_filter.md)
16. [US-016 담당자 할당 (F-301 확장)](US-016_assignees.md) - MVP에서 기본 기능 제공
17. [US-017 실시간 동기화·협업 상태](US-017_realtime_collaboration.md)
18. [US-018 데이터 백업/내보내기](US-018_data_backup_and_export.md)

### 백로그/확장 (P1+)
- 공개 API(써드파티 연동): `story_map.md` 12절 참조
- 다국어 확장: 추가 언어(일/중 등)
- 고급 알림 채널: 이메일/푸시 알림
- 데이터 관리 확장: 관리자 삭제 정책(승인 워크플로), 데이터 복구 서비스

## 참고
- 근거 문서: `../story_map.md`, `../PRD.md`, `../SRS.md`, `../errors.md`
- 제약: 데이터/성능/보안 요구는 각 스토리의 NFR 및 수용 기준에 요약됨
- 오류 표준: 에러 상태 코드/응답 구조는 각 스토리의 "오류 응답(표준)" 섹션과 `../errors.md`를 참조
