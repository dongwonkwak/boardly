# Boardly

<div align="center">
  <h1>🌟 Boardly</h1>
  <p>개인 및 소규모 팀을 위한 직관적인 칸반 보드 애플리케이션</p>
  
  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-18-blue.svg)](https://reactjs.org/)
  [![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
</div>

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [빠른 시작](#-빠른-시작)
- [개발 환경 설정](#-개발-환경-설정)
- [API 문서](#-api-문서)
- [다국어 지원](#-다국어-지원)
- [배포](#-배포)
- [라이센스](#-라이센스)

## 🎯 프로젝트 소개

Boardly는 개인 개발자와 소규모 팀이 작업을 시각적으로 관리하고 협업할 수 있는 칸반(Kanban) 보드 스타일의 웹 애플리케이션입니다. 직관적인 드래그 앤 드롭 인터페이스와 강력한 기능들로 프로젝트 관리를 더욱 효율적으로 만들어줍니다.

### 타겟 사용자
- **개인 개발자**: 체계적인 작업 관리가 필요한 개인 개발자
- **소규모 팀**: 프로젝트 진행 상황을 공유하고 싶은 소규모 스터디 또는 팀
- **일반 사용자**: 개인적인 목표나 할 일을 시각적으로 정리하고 싶은 학생 및 일반 사용자

## ✨ 주요 기능

### 🔐 사용자 관리
- **안전한 인증**: OAuth2 PKCE 플로우를 통한 보안 강화된 로그인
- **프로필 관리**: 개인정보 수정, 비밀번호 변경, 언어 설정
- **계정 관리**: 계정 삭제 및 데이터 완전 제거

### 📊 보드 관리
- **보드 CRUD**: 생성, 조회, 수정, 삭제 (사용자당 최대 50개)
- **보드 복제**: 기존 보드 구조를 복사하여 새 보드 생성
- **권한 관리**: 사용자별 보드 접근 권한 관리

### 📝 리스트 및 카드 관리
- **리스트 관리**: 색상 지원, 순서 변경, CRUD 기능 (보드당 최대 20개)
- **카드 관리**: 마크다운 지원, 복제 기능, CRUD 기능 (리스트당 최대 100개)
- **드래그 앤 드롭**: 직관적인 카드 이동 및 리스트 순서 변경

### 🌐 추가 기능
- **다국어 지원**: 한국어/영어 지원
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 완벽 지원
- **실시간 업데이트**: 협업을 위한 실시간 데이터 동기화
- **사용자 활동 로그**: 모든 활동 추적 및 로깅

## 🛠 기술 스택

### Backend
| 기술 | 버전 | 설명 |
|------|------|------|
| **Java** | 21 | OpenJDK 21 LTS |
| **Spring Boot** | 3.5.3 | 메인 프레임워크 |
| **Spring Security** | 6.x | 인증 및 권한 관리 |
| **Spring Data JPA** | 3.x | 데이터 액세스 계층 |
| **PostgreSQL** | Latest | 주 데이터베이스 |
| **H2** | 2.3.232 | 테스트 데이터베이스 |
| **SpringDoc OpenAPI** | 2.8.9 | API 문서화 |

### Frontend
| 기술 | 버전 | 설명 |
|------|------|------|
| **React** | 18 | UI 라이브러리 |
| **TypeScript** | 5.x | 타입 안전성 |
| **Vite** | Latest | 빌드 도구 |
| **Tailwind CSS** | Latest | CSS 프레임워크 |
| **shadcn/ui** | Latest | UI 컴포넌트 라이브러리 |
| **React Router** | 6.x | 라우팅 |
| **React Hook Form** | Latest | 폼 관리 |
| **Zod** | Latest | 스키마 검증 |
| **react-i18next** | Latest | 다국어 지원 |

### DevOps & Tools
- **Gradle** - 백엔드 빌드 도구
- **pnpm** - 프론트엔드 패키지 매니저
- **Docker** - 컨테이너화
- **GitHub Actions** - CI/CD
- **ESLint & Prettier** - 코드 품질 관리

## 📁 프로젝트 구조

```
boardly/
├── backend/                    # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/java/com/boardly/
│   │   │   ├── features/       # 기능별 모듈
│   │   │   │   ├── auth/       # 인증 관리
│   │   │   │   ├── user/       # 사용자 관리
│   │   │   │   ├── board/      # 보드 관리
│   │   │   │   ├── boardlist/  # 리스트 관리
│   │   │   │   ├── card/       # 카드 관리
│   │   │   │   └── dashboard/  # 대시보드
│   │   │   └── shared/         # 공통 모듈
│   │   └── main/resources/
│   ├── build.gradle            # 빌드 설정
│   └── README.md              # 백엔드 상세 문서
│
├── frontend/                   # React 프론트엔드
│   ├── src/
│   │   ├── components/         # 재사용 가능한 컴포넌트
│   │   ├── pages/             # 페이지 컴포넌트
│   │   ├── hooks/             # 커스텀 훅
│   │   ├── services/          # API 서비스
│   │   ├── stores/            # 상태 관리
│   │   ├── types/             # TypeScript 타입
│   │   └── utils/             # 유틸리티 함수
│   ├── package.json
│   └── README.md              # 프론트엔드 상세 문서
│
├── docs/                       # 프로젝트 문서
│   ├── api/                   # API 문서
│   ├── prd.md                 # 제품 요구사항 정의서
│   ├── plan.md                # 개발 계획서
│   └── activity.md            # 활동 로그 명세
│
├── docker-compose.yml          # 개발 환경 설정
├── .gitignore
└── README.md                  # 메인 문서 (현재 파일)
```

## 🚀 빠른 시작

### 사전 요구사항
- **Java 21** 이상
- **Node.js 18** 이상
- **PostgreSQL 12** 이상
- **Docker & Docker Compose** (선택사항)

### 1. 저장소 클론
```bash
git clone https://github.com/dongwonkwak/boardly.git
cd boardly
```

### 2. Docker Compose를 이용한 실행 (권장)
```bash
# 데이터베이스 및 전체 애플리케이션 실행
docker-compose up profile dev up -d

# 애플리케이션 접속
# - 프론트엔드: http://localhost:5123
# - 백엔드 API: http://localhost:8080
# - API 문서: http://localhost:8080/swagger-ui.html
```

### 3. 개별 실행

#### 백엔드 실행
```bash
cd backend

# PostgreSQL 데이터베이스 실행 (Docker)
docker run --name boardly-db -e POSTGRES_DB=boardly -e POSTGRES_USER=boardly -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:latest

# 애플리케이션 실행
./gradlew bootRun
```

#### 프론트엔드 실행
```bash
cd frontend

# 의존성 설치
pnpm install

# 개발 서버 시작
pnpm dev
```

## 🔧 개발 환경 설정

### 환경 변수 설정

#### 백엔드 (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/boardly
    username: boardly
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

#### 프론트엔드 (.env.local)
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_OAUTH_CLIENT_ID=your-client-id
VITE_OAUTH_REDIRECT_URI=http://localhost:5123/callback
```

### 개발 도구 설정

#### 백엔드
```bash
cd backend

# 코드 포맷팅
./gradlew spotlessApply

# 테스트 실행
./gradlew test

# API 문서 생성
./gradlew generateOpenApiDocs
```

#### 프론트엔드
```bash
cd frontend

# 코드 포맷팅
pnpm format

# 린팅
pnpm lint

# 타입 체크
pnpm type-check

# 테스트 실행
pnpm test
```

## 📚 API 문서

### OpenAPI 3.0 문서
- **개발 환경**: http://localhost:8080/swagger-ui.html
- **API JSON**: http://localhost:8080/api-docs
- **정적 문서**: [docs/api/openapi.json](docs/api/openapi.json)


## 🌐 다국어 지원

Boardly는 다음 언어를 지원합니다:
- **한국어 (ko)**: 기본 언어
- **English (en)**: 영어

### 번역 파일 위치
```
frontend/src/assets/locales/
├── ko/
│   ├── common.json
│   ├── activity.json
└── en/
    ├── common.json
    └── activity.json
```

## 🏗 배포

### 프로덕션 빌드
```bash
# 백엔드 빌드
cd backend && ./gradlew build

# 프론트엔드 빌드
cd frontend && pnpm build
```

### Docker 이미지 빌드
```bash
# 백엔드 이미지
docker build -t boardly-backend ./backend

# 프론트엔드 이미지
docker build -t boardly-frontend ./frontend
```


### 환경별 배포 설정
- **개발 환경**: Docker Compose
```bash
docker-compose --profile dev up -d
```

### 개발 가이드라인
- **코딩 컨벤션**: Google Java Style Guide, Airbnb JavaScript Style Guide
- **커밋 메시지**: Conventional Commits 규칙 준수
- **테스트**: 새로운 기능에 대한 테스트 코드 필수
- **문서화**: README 및 API 문서 최신 상태 유지

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 📞 문의 및 지원

- **GitHub Issues**: 버그 리포트 및 기능 요청
- **Email**: dongwon.kwak@gmail.com

---

<div align="center">
  Made with ❤️ by the Boardly Team
  <br>
  <sub>개인 및 소규모 팀을 위한 최고의 칸반 보드 솔루션</sub>
</div>