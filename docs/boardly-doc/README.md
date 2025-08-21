# Boardly - 협업 프로젝트 관리 도구

칸반 보드 기반의 직관적이고 강력한 프로젝트 관리 서비스

## 📋 프로젝트 개요

Boardly는 개인과 팀이 효율적으로 프로젝트를 관리할 수 있는 웹 기반 칸반 보드 도구입니다. 직관적인 드래그 앤 드롭 인터페이스와 강력한 협업 기능을 제공합니다.

### 🌟 주요 기능

- **워크스페이스 관리**: 개인/팀 워크스페이스 생성 및 관리
- **칸반 보드**: 시각적 프로젝트 관리 (할 일 → 진행중 → 완료)
- **실시간 협업**: 팀원과 실시간으로 작업 공유 및 소통
- **권한 관리**: 세분화된 권한 체계로 안전한 협업
- **외부 초대**: 클라이언트나 파트너를 특정 보드에 초대
- **댓글 시스템**: 작업 항목별 커뮤니케이션
- **라벨 관리**: 우선순위 및 카테고리 분류

## 🏗️ 프로젝트 구조

```
boardly-doc/
├── docs/                          # 📚 설계 문서
│   ├── domain-analysis/           # 도메인 분석
│   │   ├── domain-model.md       # 도메인 모델 정의
│   │   └── permission-policy.md  # 권한 정책
│   ├── user-stories/              # 사용자 스토리
│   │   ├── workspace-management.md
│   │   ├── board-management.md
│   │   ├── card-management.md
│   │   ├── collaboration.md
│   │   └── external-invitation.md
│   ├── api/                       # API 설계 문서
│   │   ├── openapi.yaml          # OpenAPI 3.0 스펙
│   │   ├── paths/                # API 엔드포인트
│   │   ├── schemas/              # 데이터 스키마
│   │   └── README.md             # API 문서 가이드
│   ├── PROJECT_CONTEXT.md         # 프로젝트 컨텍스트
│   └── README.md                 # 문서 가이드
├── .cursorrules                   # Cursor AI 컨텍스트
└── README.md                     # 이 파일
```

## 🚀 빠른 시작

### 문서 탐색

1. **프로젝트 이해**: [`docs/PROJECT_CONTEXT.md`](docs/PROJECT_CONTEXT.md) 읽기
2. **도메인 모델**: [`docs/domain-analysis/domain-model.md`](docs/domain-analysis/domain-model.md) 확인
3. **권한 정책**: [`docs/domain-analysis/permission-policy.md`](docs/domain-analysis/permission-policy.md) 검토
4. **API 설계**: [`docs/api/README.md`](docs/api/README.md) 참조

### API 문서 생성

```bash
cd docs/api
chmod +x generate-docs.sh
./generate-docs.sh
```

## 🎯 개발 로드맵

### Phase 1: 핵심 기능 (MVP)
- [x] 프로젝트 설계 및 문서화
- [ ] 사용자 인증 (OAuth2 + PKCE)
- [ ] 워크스페이스 및 보드 관리
- [ ] 기본 카드 관리 (생성, 수정, 이동)
- [ ] 권한 기반 접근 제어

### Phase 2: 협업 기능
- [ ] 실시간 업데이트 (WebSocket)
- [ ] 댓글 및 소통 기능
- [ ] 담당자 할당 및 라벨링
- [ ] 외부 사용자 초대 시스템

### Phase 3: 고도화
- [ ] 체크리스트 및 첨부 파일
- [ ] 알림 시스템
- [ ] 활동 로그 및 대시보드
- [ ] 모바일 반응형 지원

## 🛠️ 기술 스택

### Frontend
- **Framework**: React + TypeScript
- **UI Library**: shadcn/ui
- **State Management**: React Query (TanStack Query)
- **Build Tool**: Vite

### Backend
- **Runtime**: Node.js (Express) 또는 Java (Spring Boot)
- **Database**: PostgreSQL 또는 MySQL
- **Authentication**: OAuth2 + PKCE, JWT
- **Documentation**: OpenAPI 3.0

### Infrastructure
- **Containerization**: Docker
- **CI/CD**: GitHub Actions
- **Deployment**: Cloud Platform (AWS/GCP/Azure)

## 📖 설계 철학

### 사용자 중심 설계
- 직관적이고 학습 비용이 낮은 인터페이스
- 다양한 팀 규모와 업무 스타일에 적응 가능
- 접근성과 사용성을 우선시

### 보안 우선
- 세분화된 권한 관리 체계
- OAuth2 기반 안전한 인증
- 데이터 암호화 및 보안 감사

### 확장성 고려
- 마이크로서비스 아키텍처 준비
- API 우선 설계로 서드파티 연동 지원
- 수평 확장 가능한 구조

## 🤝 기여하기

### 문서 기여
1. 설계 문서 개선 제안
2. 사용자 스토리 추가
3. API 스펙 검토 및 피드백

### 개발 기여
1. 이슈 리포팅
2. 기능 제안
3. 코드 기여 (PR 환영)

## 📞 연락처

- **프로젝트 관리자**: [이름](mailto:email@example.com)
- **이슈 트래킹**: GitHub Issues
- **문서 피드백**: GitHub Discussions

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일 참조

---

**Boardly**와 함께 더 효율적인 프로젝트 관리를 경험해보세요! 🚀
