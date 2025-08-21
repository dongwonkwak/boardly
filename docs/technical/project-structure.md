# 📁 프로젝트 구조 가이드

## 🏗️ 전체 프로젝트 구조

```
boardly-doc/                           # 🏠 프로젝트 루트
├── 📚 docs/                          # 프로젝트 문서
│   ├── api/                          # API 설계 문서
│   │   ├── openapi.yaml              # 메인 OpenAPI 스펙
│   │   ├── paths/                    # API 엔드포인트 정의
│   │   ├── schemas/                  # 데이터 스키마
│   │   ├── parameters/               # 공통 파라미터
│   │   ├── responses/                # 공통 응답
│   │   └── generate-docs.sh          # 문서 생성 스크립트
│   ├── domain-analysis/              # 도메인 분석
│   │   ├── domain-model.md           # 도메인 모델
│   │   └── permission-policy.md      # 권한 정책
│   ├── user-stories/                 # 사용자 스토리
│   │   └── board-management.md       # 보드 관리 스토리
│   ├── technical/                    # 기술 문서
│   │   ├── project-structure.md      # 이 파일
│   │   └── internationalization.md   # 국제화 전략
│   └── README.md                     # 문서 가이드
├── 🖥️ backend/                      # Spring Boot 백엔드
│   ├── boardly-domain/               # 도메인 모듈
│   │   ├── src/main/java/
│   │   │   └── com/boardly/domain/
│   │   │       ├── user/             # User 도메인
│   │   │       ├── workspace/        # Workspace 도메인
│   │   │       ├── board/            # Board 도메인
│   │   │       └── common/           # 공통 도메인
│   │   └── build.gradle
│   ├── boardly-application/          # 애플리케이션 모듈
│   │   ├── src/main/java/
│   │   │   └── com/boardly/application/
│   │   │       ├── service/          # 서비스 레이어
│   │   │       ├── dto/              # DTO 클래스
│   │   │       └── usecase/          # 유스케이스
│   │   └── build.gradle
│   ├── boardly-infrastructure/       # 인프라 모듈
│   │   ├── src/main/java/
│   │   │   └── com/boardly/infrastructure/
│   │   │       ├── database/         # 데이터베이스 설정
│   │   │       │   ├── jpa/          # JPA Repository
│   │   │       │   ├── querydsl/     # QueryDSL
│   │   │       │   └── migration/    # Flyway 스크립트
│   │   │       ├── storage/          # 파일 저장소
│   │   │       ├── cache/            # Redis 캐시
│   │   │       └── websocket/        # WebSocket 설정
│   │   └── build.gradle
│   ├── boardly-web/                  # 웹 모듈
│   │   ├── src/main/java/
│   │   │   └── com/boardly/web/
│   │   │       ├── controller/       # REST 컨트롤러
│   │   │       ├── config/           # 웹 설정
│   │   │       └── exception/        # 예외 처리
│   │   └── build.gradle
│   ├── boardly-test/                 # 테스트 모듈
│   │   └── src/test/java/
│   ├── build.gradle                  # 루트 build.gradle
│   ├── settings.gradle               # 멀티모듈 설정
│   └── gradle/                       # Gradle Wrapper
├── 🎨 frontend/                      # React 프론트엔드
│   ├── src/
│   │   ├── components/               # UI 컴포넌트
│   │   │   ├── ui/                   # shadcn/ui 컴포넌트
│   │   │   ├── board/                # 보드 관련 컴포넌트
│   │   │   ├── workspace/            # 워크스페이스 컴포넌트
│   │   │   └── common/               # 공통 컴포넌트
│   │   ├── hooks/                    # 커스텀 훅
│   │   ├── stores/                   # Zustand 스토어
│   │   ├── services/                 # API 서비스
│   │   ├── types/                    # TypeScript 타입
│   │   ├── utils/                    # 유틸리티 함수
│   │   ├── i18n/                     # 국제화 설정
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/                       # 정적 파일
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── tsconfig.json
├── 🔄 shared/                        # 공통 리소스 (선택사항)
│   ├── types/                        # 공통 TypeScript 타입
│   │   ├── api.ts                    # API 응답 타입
│   │   └── domain.ts                 # 도메인 타입
│   └── constants/                    # 공통 상수
│       └── api.ts                    # API 엔드포인트
├── 🛠️ scripts/                      # 빌드/배포 스크립트
│   ├── build.sh                      # 전체 빌드 스크립트
│   ├── deploy.sh                     # 배포 스크립트
│   └── setup.sh                      # 개발 환경 설정
├── 🐳 docker/                        # Docker 설정
│   ├── backend/
│   │   └── Dockerfile
│   ├── frontend/
│   │   └── Dockerfile
│   └── docker-compose.yml
├── 📋 .github/                       # GitHub Actions
│   └── workflows/
│       ├── ci.yml                    # CI 파이프라인
│       └── deploy.yml                # 배포 파이프라인
├── 📄 README.md                      # 프로젝트 개요
├── 📦 package.json                   # 루트 package.json (workspace)
├── 📋 .cursorrules                   # Cursor AI 설정
└── 📋 .cursorignore                  # Cursor AI 제외 파일
```

## 🎯 각 폴더의 역할

### 📚 **docs/** - 프로젝트 문서
- **목적**: 프로젝트 설계, API 문서, 가이드라인
- **대상**: 개발자, 기획자, 문서화
- **특징**: 설계 단계에서 작성된 문서들

### 🖥️ **backend/** - Spring Boot 백엔드
- **목적**: REST API, 비즈니스 로직, 데이터 처리
- **아키텍처**: Hexagonal Architecture (Ports & Adapters)
- **모듈**: Domain, Application, Infrastructure, Web

### 🎨 **frontend/** - React 프론트엔드
- **목적**: 사용자 인터페이스, 상태 관리, API 호출
- **기술**: React 18+, TypeScript, Vite, Zustand
- **UI**: shadcn/ui + Tailwind CSS

### 🔄 **shared/** - 공통 리소스 (선택사항)
- **목적**: Backend와 Frontend 간 공통 타입/상수
- **장점**: 타입 안전성, 중복 제거
- **단점**: 복잡성 증가

### 🛠️ **scripts/** - 자동화 스크립트
- **목적**: 빌드, 배포, 개발 환경 설정 자동화
- **예시**: 전체 프로젝트 빌드, Docker 실행

### 🐳 **docker/** - 컨테이너 설정
- **목적**: 개발/운영 환경 컨테이너화
- **구성**: Backend, Frontend, Database 컨테이너

## 🚀 개발 워크플로우

### 1. **초기 설정**
```bash
# 1. Backend 프로젝트 생성
cd backend
./gradlew build

# 2. Frontend 프로젝트 생성  
cd ../frontend
npm install
npm run dev

# 3. 전체 프로젝트 빌드
cd ..
./scripts/build.sh
```

### 2. **개발 과정**
```bash
# Backend 개발
cd backend
./gradlew bootRun

# Frontend 개발
cd frontend  
npm run dev

# 전체 테스트
./scripts/test.sh
```

### 3. **배포**
```bash
# Docker 빌드
docker-compose build

# 배포
./scripts/deploy.sh
```

## 💡 장점

1. **통합 관리**: 한 저장소에서 전체 프로젝트 관리
2. **타입 공유**: Backend API 타입을 Frontend에서 재사용
3. **CI/CD 통합**: 전체 프로젝트 빌드/테스트/배포
4. **개발 편의성**: IDE에서 전체 프로젝트 탐색
5. **버전 동기화**: Backend/Frontend 버전 관리 용이

## ⚠️ 고려사항

1. **저장소 크기**: 문서 + 코드로 저장소가 커질 수 있음
2. **권한 관리**: Backend/Frontend 팀별 접근 권한 설정 필요
3. **빌드 시간**: 전체 프로젝트 빌드 시 시간 소요
4. **복잡성**: 초기 설정이 다소 복잡할 수 있음

## 🎯 결론

**Monorepo 구조를 추천**합니다. 특히 Boardly 같은 중소규모 프로젝트에서는:

- 개발 효율성 향상
- 타입 안전성 보장  
- 통합 관리 편의성
- 팀 협업 개선

의 장점이 단점을 충분히 상쇄합니다.
