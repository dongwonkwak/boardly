# Boardly - 칸반 보드 기반 협업 프로젝트 관리 서비스

## 🚀 프로젝트 개요

Boardly는 팀과 개인이 효율적으로 프로젝트를 관리할 수 있는 칸반 보드 기반의 협업 도구입니다.

### 주요 기능
- **워크스페이스 관리**: 팀별 작업 공간 생성 및 관리
- **칸반 보드**: 드래그 앤 드롭으로 작업 흐름 관리
- **실시간 협업**: 팀원들과 동시 편집
- **외부 초대**: 게스트 사용자 초대 및 권한 관리
- **활동 로그**: 작업 히스토리 추적

## 🏗️ 프로젝트 구조

```
boardly/                           # 🏠 프로젝트 루트
├── 📚 docs/                      # 프로젝트 문서
│   ├── api/                      # API 설계 문서
│   ├── domain-analysis/          # 도메인 분석
│   ├── user-stories/             # 사용자 스토리
│   └── technical/                # 기술 문서
├── 🖥️ backend/                  # Spring Boot 백엔드
├── 🎨 frontend/                  # React 프론트엔드
├── 🔄 shared/                    # 공통 리소스
├── 🛠️ scripts/                  # 빌드/배포 스크립트
├── 🐳 docker/                    # Docker 설정
└── 📋 .github/                   # GitHub Actions
```

## 🛠️ 기술 스택

### Backend
- **Java**: 21
- **Framework**: Spring Boot 3.x
- **Architecture**: Hexagonal Architecture
- **Database**: PostgreSQL (운영), H2 (개발)
- **ORM**: Spring Data JPA + QueryDSL
- **Authentication**: OAuth2 + JWT
- **Documentation**: OpenAPI 3.0

### Frontend
- **Framework**: React 18+ with TypeScript
- **Build Tool**: Vite
- **State Management**: Zustand
- **UI Library**: shadcn/ui
- **Form Validation**: Zod
- **HTTP Client**: Axios
- **Internationalization**: react-i18next

## 🚀 빠른 시작

### Prerequisites
- Java 21
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL (운영 환경)

### 개발 환경 설정

1. **저장소 클론**
```bash
git clone <repository-url>
cd boardly
```

2. **Backend 실행**
```bash
cd backend
./gradlew bootRun
```

3. **Frontend 실행**
```bash
cd frontend
npm install
npm run dev
```

4. **Docker로 전체 실행**
```bash
docker-compose up -d
```

## 📖 문서

- [API 문서](./docs/api/README.md)
- [도메인 분석](./docs/domain-analysis/)
- [사용자 스토리](./docs/user-stories/)
- [기술 문서](./docs/technical/)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 연락처

프로젝트 링크: [https://github.com/your-username/boardly](https://github.com/your-username/boardly)
