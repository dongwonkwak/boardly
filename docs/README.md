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
│   ├── hexagonal-architecture-guide.md # 헥사고날 아키텍처 가이드
│   ├── api-response-standards.md       # API 응답 형식 표준
│   └── error-handling-standards.md     # 에러 처리 표준
├── implementation/             # 구현 가이드
│   ├── usecase-pattern-guide.md        # UseCase 패턴 가이드
│   ├── input-validation-guide.md       # 입력 검증 표준
│   ├── testing-guide.md                # 테스트 작성 가이드
│   └── dependency-management-guide.md  # 의존성 관리 가이드
├── technical/                  # 기술적 세부사항
│   ├── project-structure.md    # 프로젝트 구조
│   ├── internationalization.md # 국제화
│   └── coding-standards.md     # 코딩 표준
└── api/                        # API 문서
    ├── README.md               # API 개요
    ├── openapi.yaml            # OpenAPI 스펙
    ├── parameters/             # 공통 파라미터
    ├── paths/                  # API 경로
    ├── schemas/                # 스키마 정의
    └── responses/              # 응답 정의
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
1. 도메인 분석 문서 작성
2. 아키텍처 설계 문서 작성
3. 배포 가이드 문서 작성
4. 모니터링 및 로깅 가이드 작성
