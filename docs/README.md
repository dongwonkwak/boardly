# Boardly 프로젝트 문서

## 개요
Boardly는 트렐로 유사한 칸반 보드 서비스입니다. 이 문서는 스토리기반 + DDD(도메인 주도 설계) 개발을 위한 문서들을 포함합니다.

## 문서 구조

```
docs/
├── README.md                    # 이 파일
├── user-stories/               # 유저스토리와 수용기준
│   ├── board-management.md     # 보드 관리 관련 스토리
│   ├── card-management.md      # 카드 관리 관련 스토리
│   ├── workspace-management.md # 워크스페이스 관리 관련 스토리
│   ├── collaboration.md        # 협업 기능 관련 스토리
│   └── external-invitation.md  # 외부 초대 관련 스토리
├── domain-analysis/            # 도메인 분석 문서
│   ├── domain-model.md         # 도메인 모델 정의
│   ├── permission-policy.md    # 권한 정책 정의
│   ├── bounded-contexts.md     # 바운디드 컨텍스트 정의
│   └── domain-events.md        # 도메인 이벤트 정의
├── architecture/               # 아키텍처 설계 문서
│   ├── system-overview.md      # 시스템 전체 개요
│   ├── api-design.md           # API 설계
│   └── database-schema.md      # 데이터베이스 스키마
└── implementation/             # 구현 가이드
    ├── coding-standards.md     # 코딩 표준
    ├── testing-strategy.md     # 테스트 전략
    └── deployment-guide.md     # 배포 가이드
```

## 문서 작성 규칙

### 유저스토리 형식
- **As a** [역할]
- **I want to** [원하는 기능]
- **So that** [목적]

### 수용기준 형식
- **Given** [전제 조건]
- **When** [행동]
- **Then** [결과]

### 비즈니스 규칙
- 명확하고 검증 가능한 규칙으로 작성
- 예외 상황도 포함

## 현재 상태
- ✅ 보드 관리 유저스토리 작성 완료
- ✅ 카드 관리 유저스토리 작성 완료
- ✅ 워크스페이스 관리 유저스토리 작성 완료
- ✅ 협업 기능 유저스토리 작성 완료
- ✅ 외부 초대 유저스토리 작성 완료

## 다음 단계
1. 카드 관리 관련 유저스토리 작성
2. 도메인 분석 문서 작성
3. 아키텍처 설계 문서 작성
4. 구현 가이드 문서 작성
