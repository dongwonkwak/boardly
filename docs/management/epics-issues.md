# Epic & Issues — Boardly

프로젝트의 주요 Epic과 이를 구성하는 Issue를 정의한 문서입니다. 로드맵을 기반으로 Epic을 선정하고, Epic을 세부 기능 단위 Issue로 쪼갭니다. Cycle Plan과 Task Tracker에서 참조됩니다. 다국어(i18n)는 모든 Epic에서 기본 고려사항으로 반영됩니다. 풀스택 개발(Backend + Frontend + Integration)에 맞춰 구성됩니다.

---

## Epic 1: 사용자 인증/인가 (MVP 1)

사용자 계정 생성 및 인증을 담당하는 Epic. JWT 기반 인증과 토큰 관리를 포함합니다.

### Issues

* **Issue #101: 회원가입 기능 구현** (Backend + Frontend + Integration)

  * 회원가입 API 개발 (DTO, Entity, Service, Controller)
  * 회원가입 UI 개발 (폼, validation, 상태관리)
  * API 연동 및 E2E 테스트 (회원가입 성공/실패 시나리오)

* **Issue #102: 로그인 기능 구현** (Backend + Frontend + Integration)

  * 로그인 API 개발 (JWT 토큰 발급)
  * 로그인 UI 개발 (폼, 인증 상태관리)
  * 토큰 저장 및 인증 플로우 통합 테스트 (성공/실패/만료 토큰)

* **Issue #103: Refresh Token 갱신 기능 구현** (Backend + Frontend + Integration)

  * Refresh Token 관리 API 개발
  * 토큰 자동 갱신 로직 구현 (Frontend)
  * 토큰 만료/갱신 시나리오 E2E 테스트

---

## Epic 2: 워크스페이스 관리 (MVP 2)

워크스페이스 생성, 멤버 관리 및 권한 제어를 담당하는 Epic.

### Issues

* **Issue #201: 워크스페이스 생성 기능 구현** (Backend + Frontend + Integration)

  * 워크스페이스 CRUD API 개발
  * 워크스페이스 생성 UI 개발 (모달/폼)
  * 생성 후 목록 갱신 및 네비게이션 통합 테스트

* **Issue #202: 워크스페이스 목록/조회 기능 구현** (Backend + Frontend + Integration)

  * 워크스페이스 목록/상세 조회 API 개발
  * 워크스페이스 목록 UI 개발 (카드 레이아웃)
  * 목록 조회 및 페이지네이션 통합 테스트

* **Issue #203: 워크스페이스 수정/삭제 기능 구현** (Backend + Frontend + Integration)

  * 워크스페이스 수정/삭제 API 개발
  * 수정/삭제 UI 개발 (모달, 확인 다이얼로그)
  * 수정/삭제 후 상태 동기화 통합 테스트

* **Issue #204: 워크스페이스 멤버 관리 기능 구현** (Backend + Frontend + Integration)

  * 멤버 초대/추가/삭제 API 개발
  * 이메일 초대 시스템 구현
  * 멤버 관리 UI 개발 (초대 모달, 멤버 목록)
  * 초대 플로우 및 멤버 관리 통합 테스트 (이메일 초대 발송/수락)

* **Issue #205: 워크스페이스 권한(Role) 관리 기능 구현** (Backend + Frontend + Integration)

  * RBAC 시스템 구현 (OWNER/MEMBER/GUEST)
  * 권한 검증 로직 및 API 보안 처리
  * 권한별 UI 표시/제어 로직 구현
  * 권한 기반 접근 제어 통합 테스트

* **Issue #206: 워크스페이스 활동 로그/감사 로그 기능 구현** (Backend + Frontend + Integration)

  * 활동 로그 기록 시스템 구현
  * 이벤트 기반 로그 수집 (Event Driven Architecture)
  * 활동 로그 조회 UI 개발
  * 로그 기록 및 조회 통합 테스트

---

## Epic 3: 보드 관리 (MVP 2)

워크스페이스 내 보드를 생성/조회/수정/삭제하고, 멤버 및 권한을 관리하는 기능.

### Issues

* **Issue #301: 보드 생성 기능 구현** (Backend + Frontend + Integration)

  * 보드 생성 API 개발 (Entity, Service, Controller)
  * 보드 생성 UI 개발 (템플릿, 설정 옵션)
  * 보드 생성 후 네비게이션 및 초기 설정 통합 테스트

* **Issue #302: 보드 조회/목록 기능 구현** (Backend + Frontend + Integration)

  * 보드 목록/상세 조회 API 개발
  * 보드 목록 UI 개발 (그리드/리스트 뷰)
  * 보드 상세 페이지 구현
  * 보드 조회 및 페이지 라우팅 통합 테스트

* **Issue #303: 보드 수정/삭제 기능 구현** (Backend + Frontend + Integration)

  * 보드 수정/삭제 API 개발
  * 보드 설정 UI 개발 (이름, 설명, 배경 등)
  * 보드 삭제 확인 및 복구 옵션 UI
  * 보드 수정/삭제 및 상태 동기화 통합 테스트

* **Issue #304: 보드 멤버 관리 기능 구현** (Backend + Frontend + Integration)

  * 보드 멤버 초대/추가/삭제 API 개발
  * 보드별 멤버 관리 UI 개발
  * 멤버 초대 링크 생성/공유 기능
  * 보드 멤버 관리 통합 테스트

* **Issue #305: 보드 권한(Role) 관리 기능 구현** (Backend + Frontend + Integration)

  * 보드 RBAC 시스템 구현 (ADMIN/MEMBER/VIEWER)
  * 보드별 권한 검증 및 API 보안
  * 권한별 보드 UI 제어 로직
  * 보드 권한 관리 통합 테스트

* **Issue #306: 보드 활동 로그/감사 로그 기능 구현** (Backend + Frontend + Integration)

  * 보드별 활동 로그 수집 시스템
  * 보드 변경 이벤트 추적 및 기록
  * 보드 활동 피드 UI 개발
  * 보드 활동 로그 통합 테스트

---

## Epic 4: 카드/리스트 관리 (MVP 3)

보드 내 리스트와 카드 CRUD 및 이동 기능. 드래그앤드롭과 실시간 업데이트를 포함합니다.

### Issues

* **Issue #401: 리스트 생성/수정/삭제 기능 구현** (Backend + Frontend + Integration)

  * 리스트 CRUD API 개발 (순서 관리 포함)
  * 리스트 UI 컴포넌트 개발
  * 리스트 드래그앤드롭 구현 (@dnd-kit)
  * 리스트 관리 및 순서 변경 통합 테스트

* **Issue #402: 카드 생성/수정/삭제 기능 구현** (Backend + Frontend + Integration)

  * 카드 CRUD API 개발 (상세 정보 포함)
  * 카드 UI 컴포넌트 개발
  * 카드 생성/수정 모달 구현
  * 카드 상세 정보 관리 통합 테스트

* **Issue #403: 카드 드래그&드롭 이동 처리** (Backend + Frontend + Integration)

  * 카드 이동 API 개발 (리스트 간, 순서 변경)
  * 카드 드래그앤드롭 UI 구현 (@dnd-kit)
  * WebSocket을 통한 실시간 업데이트
  * 실시간 카드 이동 및 동기화 E2E 테스트

* **Issue #404: 카드 상세 조회 기능 구현** (Backend + Frontend + Integration)

  * 카드 상세 정보 조회 API 개발
  * 카드 상세 모달/페이지 UI 개발
  * 카드 메타데이터 관리 (라벨, 마감일, 담당자)
  * 카드 상세 정보 통합 테스트

* **Issue #405: 카드 첨부 파일 업로드/다운로드 기능 구현** (Backend + Frontend + Integration)

  * 파일 업로드/다운로드 API 개발 (S3/로컬 저장소)
  * 파일 첨부 UI 컴포넌트 개발
  * 드래그앤드롭 파일 업로드 구현
  * 파일 관리 및 저장소 연동 통합 테스트

* **Issue #406: 카드/리스트 검색 및 필터링 기능 구현** (Backend + Frontend + Integration)

  * 검색/필터링 API 개발 (키워드, 라벨, 담당자)
  * 검색 UI 컴포넌트 개발 (검색바, 필터 옵션)
  * 실시간 검색 및 자동완성 구현
  * 검색 성능 최적화 및 통합 테스트

* **Issue #407: 카드 댓글 시스템 구현** (Backend + Frontend + Integration)

  * 댓글 CRUD API 개발
  * 댓글 UI 컴포넌트 개발 (작성, 수정, 삭제)
  * 댓글 알림 및 멘션 기능
  * 댓글 시스템 통합 테스트

---

## Epic 5: 다국어(i18n) 확장 (MVP 4)

다국어 지원 고도화 및 사용자 설정 기능 제공.

### Issues

* **Issue #501: 사용자 언어 전환 기능 구현** (Backend + Frontend + Integration)

  * 사용자 언어 설정 API 개발
  * 언어 선택 UI 컴포넌트 개발
  * React i18n 라이브러리 통합 (react-i18next)
  * 언어 전환 및 영속화 E2E 테스트

* **Issue #502: 다국어 리소스 관리 및 배포 전략** (Backend + Frontend + Integration)

  * 다국어 리소스 관리 시스템 구현
  * 번역 키 자동 추출 및 관리 도구 개발
  * 번역 파일 버전 관리 및 배포 시스템
  * 리소스 관리 및 동기화 통합 테스트

* **Issue #503: 에러 메시지 및 도메인 메시지 번역 범위 확대** (Backend + Frontend + Integration)

  * 백엔드 에러 메시지 국제화 처리
  * 프론트엔드 UI 텍스트 번역 확장
  * 동적 메시지 번역 시스템 구현
  * 다국어 메시지 품질 및 일관성 테스트

* **Issue #504: 프론트엔드/백엔드 i18n 동기화 강화** (Backend + Frontend + Integration)

  * 번역 키 동기화 시스템 구현
  * 번역 누락 감지 및 알림 시스템
  * 번역 품질 관리 도구 개발
  * i18n 동기화 및 품질 관리 통합 테스트

---

## Epic 6: 알림(Notification) 시스템 (MVP 4)

사용자 이벤트에 대한 알림 기능.

### Issues

* **Issue #601: 이메일 알림 기능 구현** (Backend + Frontend + Integration)

  * 이메일 알림 발송 시스템 구현 (초대, 카드 배정, 댓글 멘션)
  * 알림 템플릿 관리 시스템 개발
  * 이메일 알림 설정 UI 개발
  * 이메일 발송 및 설정 관리 E2E 테스트

* **Issue #602: In-App Notification 기능 구현** (Backend + Frontend + Integration)

  * 실시간 알림 API 개발 (WebSocket 기반)
  * 알림 센터 UI 컴포넌트 개발
  * 알림 읽음 상태 및 관리 기능
  * 실시간 알림 및 상태 관리 E2E 테스트

* **Issue #603: 알림 설정 관리 기능 구현** (Backend + Frontend + Integration)

  * 사용자별 알림 설정 API 개발
  * 알림 설정 UI 개발 (이메일/푸시 알림 on/off)
  * 알림 빈도 및 카테고리 설정 기능
  * 알림 설정 관리 통합 테스트

---

## Epic 7: 실시간 협업 기능 (MVP 4+)

실시간 협업을 위한 WebSocket 기반 기능들.

### Issues

* **Issue #701: WebSocket 실시간 업데이트 시스템** (Backend + Frontend + Integration)

  * WebSocket + STOMP 서버 구현
  * 실시간 이벤트 브로드캐스팅 시스템
  * 프론트엔드 WebSocket 연결 관리
  * 실시간 동기화 및 연결 안정성 E2E 테스트

* **Issue #702: 실시간 사용자 현재 상태 표시** (Backend + Frontend + Integration)

  * 사용자 온라인 상태 추적 API
  * 실시간 사용자 아바타 표시 UI
  * 현재 보고 있는 카드/보드 표시 기능
  * 사용자 상태 동기화 통합 테스트

* **Issue #703: 실시간 협업 충돌 방지 시스템** (Backend + Frontend + Integration)

  * 동시 편집 감지 및 충돌 방지 로직
  * 낙관적 UI 업데이트 및 롤백 처리
  * 충돌 해결 UI 및 사용자 알림
  * 협업 충돌 처리 통합 테스트

---

## Epic 8: 운영 및 상용화 (Phase 5)

실제 서비스 운영과 상용화를 위한 관리 기능.

### Issues

* **Issue #801: 관리자 기능 구현** (Backend + Frontend + Integration)

  * 사용자 정지/복구 기능 개발
  * 초대 제한 및 이메일 도메인 제한 기능
  * 관리자 대시보드 UI 개발
  * 관리자 권한 및 기능 통합 테스트

* **Issue #802: 요금제/결제 시스템 연동** (Backend + Frontend + Integration)

  * 구독 관리 API 개발 (Payment & Subscription)
  * 결제 연동 UI 개발 (Stripe/Toss Payments)
  * 요금제별 기능 제한 로직 구현
  * 결제 및 구독 관리 E2E 테스트

* **Issue #803: 모니터링 및 운영 도구 통합** (Backend + Frontend + Integration)

  * 로그 관리 및 분석 시스템 구현
  * 운영 대시보드 및 메트릭 수집
  * 에러 추적 및 알림 시스템
  * 모니터링 및 운영 도구 통합 테스트

---

## Notes

* Epic & Issue 문서는 최신 로드맵 변경 사항을 반영해 업데이트됨
* 모든 Issue는 Backend + Frontend + Integration 작업을 포함하는 풀스택 구조
* 활동 로그, 파일 첨부, 검색/필터, 알림, 실시간 협업, 운영/결제 항목을 신규 Epic/Issue로 보강
* 각 Issue는 독립적으로 완성 가능한 기능 단위로 구성
* Cycle Plan과 Task Tracker 문서와 싱크 필요
* WebSocket 실시간 기능과 협업 충돌 방지 시스템이 새로 추가됨
