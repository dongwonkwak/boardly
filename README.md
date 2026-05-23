<div align="center">
  <h1>Boardly</h1>
  <p>개인 및 소규모 팀을 위한 칸반 보드 애플리케이션</p>

  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-19-blue.svg)](https://react.dev/)
  [![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
</div>

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [빠른 시작](#빠른-시작)
- [개발 환경 설정](#개발-환경-설정)
- [API 문서](#api-문서)
- [다국어 지원](#다국어-지원)
- [배포](#배포)
- [라이센스](#라이센스)

## 프로젝트 소개

Boardly는 개인 개발자와 소규모 팀이 작업을 시각적으로 관리하고 협업할 수 있는 칸반 보드 스타일의 웹 애플리케이션입니다. 보드, 리스트, 카드, 라벨, 멤버, 활동 로그를 중심으로 작업 흐름을 관리합니다.

### 타겟 사용자

- **개인 개발자**: 작업을 보드 단위로 정리하고 싶은 사용자
- **소규모 팀**: 프로젝트 진행 상황을 공유해야 하는 팀
- **일반 사용자**: 목표나 할 일을 시각적으로 관리하고 싶은 사용자

## 주요 기능

### 사용자 및 인증

- OAuth2 Authorization Server 기반 로그인
- PKCE가 적용된 Authorization Code 플로우
- 사용자 등록, 조회, 수정

### 보드 관리

- 보드 생성, 조회, 수정, 삭제
- 보드 즐겨찾기, 보관 처리
- 보드 멤버 및 권한 관리

### 리스트 및 카드 관리

- 보드 리스트 생성, 수정, 삭제, 위치 변경
- 카드 생성, 수정, 삭제, 이동, 복제
- 카드 담당자, 라벨, 우선순위, 완료 상태 관리

### 기타 기능

- 대시보드 통계 및 최근 활동 조회
- 사용자 활동 로그
- 한국어/영어 다국어 지원
- 반응형 UI

## 기술 스택

### Backend

| 기술 | 버전 | 설명 |
|------|------|------|
| **Java** | 21 | 메인 언어 |
| **Spring Boot** | 3.5.4 | 애플리케이션 프레임워크 |
| **Spring Security** | 6.x | 인증 및 인가 |
| **Spring Authorization Server** | Spring Boot 관리 버전 | OAuth2/OIDC 인증 서버 |
| **Spring Data JPA** | Spring Boot 관리 버전 | 데이터 액세스 |
| **Flyway** | 11.10.5 | DB 마이그레이션 |
| **PostgreSQL** | 16 | Docker 개발 DB |
| **H2** | 2.3.232 | 로컬/dev 인메모리 DB |
| **SpringDoc OpenAPI** | 2.8.9 | API 문서 |
| **Vavr** | 0.10.6 | 함수형 오류 처리 |
| **MapStruct** | 1.6.3 | DTO/도메인 매핑 |

### Frontend

| 기술 | 버전 | 설명 |
|------|------|------|
| **React** | 19.1.0 | UI 라이브러리 |
| **TypeScript** | 5.8.x | 타입 시스템 |
| **Vite** | 7.x | 개발 서버 및 빌드 |
| **Tailwind CSS** | 4.x | 스타일링 |
| **shadcn/ui 스타일 컴포넌트** | - | UI 컴포넌트 |
| **React Router DOM** | 7.x | 라우팅 |
| **Zustand** | 5.x | 클라이언트 상태 관리 |
| **react-oidc-context** | 3.x | OIDC 클라이언트 |
| **dnd-kit** | 6.x/10.x | 드래그 앤 드롭 |
| **react-i18next** | 15.x | 다국어 지원 |
| **Vitest** | 3.x | 테스트 |
| **Biome** | 2.x | 린트 및 포맷 |

### DevOps & Tools

- **Gradle Wrapper**: 백엔드 빌드
- **pnpm**: 프론트엔드 패키지 매니저
- **Docker Compose**: 로컬 통합 실행
- **OpenAPI JSON**: `docs/api/openapi.json`

## 프로젝트 구조

```text
boardly/
├── backend/                         # Spring Boot 멀티모듈 백엔드
│   ├── settings.gradle              # Gradle 모듈 설정
│   ├── build.gradle                 # 루트 빌드 및 공통 태스크
│   ├── gradle/                      # Gradle Wrapper 및 버전 카탈로그
│   ├── boardly-app/                 # 실행 애플리케이션 모듈
│   │   └── src/main/resources/      # application-*.yml
│   ├── boardly-api/                 # Controller, 요청/응답 DTO, 보안 API
│   ├── boardly-application/         # UseCase, Service, Command/Query
│   ├── boardly-domain/              # 도메인 모델, 포트, 정책 인터페이스
│   ├── boardly-infrastructure/      # JPA, Flyway, 보안/웹 설정, 어댑터
│   └── boardly-shared/              # 공통 값 객체, 검증, 에러 타입
│
├── frontend/                        # React 프론트엔드
│   ├── src/
│   │   ├── assets/locales/          # i18n 번역 파일
│   │   ├── components/              # UI, 레이아웃, 기능별 컴포넌트
│   │   ├── config/                  # i18n 등 앱 설정
│   │   ├── hooks/                   # 커스텀 훅
│   │   ├── pages/                   # 라우트 페이지
│   │   ├── providers/               # OAuth Provider
│   │   ├── services/api/            # OpenAPI 기반 API 클라이언트
│   │   ├── store/                   # Zustand 스토어
│   │   └── utils/                   # 유틸리티
│   ├── package.json
│   ├── vite.config.ts
│   ├── vitest.config.ts
│   └── README.md
│
├── docs/                            # 프로젝트 문서
│   ├── api/                         # API 문서 및 OpenAPI 스펙
│   ├── ERD.md
│   ├── prd.md
│   ├── plan.md
│   └── usecases.md
│
├── docker-compose.yml               # 로컬 통합 실행 설정
└── README.md
```

## 빠른 시작

### 사전 요구사항

- Java 21 이상
- Node.js 18 이상
- pnpm
- Docker 및 Docker Compose 선택 사항

### 1. 저장소 클론

```bash
git clone https://github.com/dongwonkwak/boardly.git
cd boardly
```

### 2. Docker Compose로 통합 실행

개발용 프론트엔드까지 함께 실행하려면 `dev` 프로필을 사용합니다.

```bash
docker compose --profile dev up -d
```

접속 주소:

- 프론트엔드(dev): http://localhost:5173
- 프론트엔드(nginx 컨테이너): http://localhost:3000
- 백엔드 API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### 3. 개별 실행

#### 백엔드 실행

기본 프로필은 `dev`이며, 현재 dev 설정은 H2 인메모리 DB와 Flyway 마이그레이션을 사용합니다.

```bash
cd backend
./gradlew :boardly-app:bootRun
```

#### 프론트엔드 실행

```bash
cd frontend
pnpm install
cp .env.example .env.development
pnpm dev
```

프론트엔드는 기본적으로 http://localhost:5173 에서 실행됩니다.

## 개발 환경 설정

### 백엔드 설정

주요 설정 파일:

- `backend/boardly-app/src/main/resources/application.yml`
- `backend/boardly-app/src/main/resources/application-dev.yml`
- `backend/boardly-app/src/main/resources/application-local.yml`

dev 프로필의 기본 OAuth2 클라이언트:

```yaml
client-id: boardly-client
client-secret: "{noop}secret"
redirect-uri: http://localhost:5173/callback
scope: openid read write
```

### 프론트엔드 환경 변수

예시는 `frontend/.env.example`에 있습니다.

```env
VITE_API_URL=http://localhost:8080
VITE_OAUTH_AUTHORIZATION_ENDPOINT=http://localhost:8080
VITE_OAUTH_CLIENT_ID=boardly-client
VITE_OAUTH_CLIENT_SECRET=secret
VITE_OAUTH_RESPONSE_TYPE=code
VITE_OAUTH_REDIRECT_URI=http://localhost:5173/callback
VITE_OAUTH_POST_LOGOUT_REDIRECT_URI=http://localhost:5173
VITE_OAUTH_SCOPE=openid read write
VITE_OAUTH_CLIENT_AUTHENTICATION=client_secret_basic
```

### 개발 명령

#### 백엔드

```bash
cd backend

# 전체 테스트
./gradlew test

# 실행 모듈만 실행
./gradlew :boardly-app:bootRun

# 전체 빌드
./gradlew build

# 통합 JaCoCo 리포트
./gradlew jacocoRootReport
```

#### 프론트엔드

```bash
cd frontend

# 개발 서버
pnpm dev

# 린트
pnpm lint

# 포맷
pnpm format

# 테스트
pnpm test:run

# 빌드
pnpm build

# OpenAPI 클라이언트 재생성
pnpm generate-api
```

## API 문서

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- 정적 OpenAPI 스펙: [docs/api/openapi.json](docs/api/openapi.json)
- API 문서 디렉터리: [docs/api](docs/api)

주요 API 영역:

- User: 사용자 등록/조회/수정
- Dashboard: 대시보드 통계 및 요약
- Board: 보드 관리
- BoardList: 리스트 관리
- Card: 카드 관리
- Label: 라벨 관리
- Activity: 활동 로그

## 다국어 지원

지원 언어:

- 한국어 (`ko`)
- 영어 (`en`)

번역 파일 위치:

```text
frontend/src/assets/locales/
├── ko/
│   ├── activity.json
│   ├── board.json
│   └── common.json
└── en/
    ├── activity.json
    ├── board.json
    └── common.json
```

## 배포

### 프로덕션 빌드

```bash
# 백엔드
cd backend
./gradlew build

# 프론트엔드
cd frontend
pnpm build
```

### Docker 이미지 빌드

```bash
docker build -t boardly-backend ./backend
docker build -t boardly-frontend ./frontend
```

### Docker Compose

```bash
# 기본 서비스 실행
docker compose up -d

# 개발용 프론트엔드 포함 실행
docker compose --profile dev up -d

# 종료
docker compose down
```

## 개발 가이드라인

- 새 기능은 가능한 한 도메인, 애플리케이션, API, 인프라 경계를 유지해서 구현합니다.
- 백엔드 변경 시 관련 서비스/유스케이스 테스트를 추가하거나 갱신합니다.
- 프론트엔드 변경 시 컴포넌트, 훅, 스토어 단위 테스트를 우선 고려합니다.
- API 계약이 바뀌면 `docs/api/openapi.json`과 프론트엔드 API 클라이언트 갱신이 필요합니다.

## 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 문의 및 지원

- GitHub Issues: 버그 리포트 및 기능 요청
- Email: dongwon.kwak@gmail.com
