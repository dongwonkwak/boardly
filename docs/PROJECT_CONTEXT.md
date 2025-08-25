# Boardly 프로젝트 컨텍스트

이 문서는 Boardly 프로젝트의 전체적인 컨텍스트와 설계 철학을 제공합니다.

## 🎯 프로젝트 비전

**"직관적이고 협업이 쉬운 칸반 보드 서비스"**

Boardly는 개인과 팀이 프로젝트를 효율적으로 관리할 수 있는 칸반 보드 기반의 협업 도구입니다.

## 🏗️ 아키텍처 개요

### 시스템 구성
```
Frontend (React + TypeScript)
    ↕ (REST API + WebSocket)
Backend (Node.js/Spring Boot)
    ↕
Database (PostgreSQL/MySQL)
```

### 주요 서비스
1. **사용자 관리**: OAuth2 인증, 프로필 관리
2. **워크스페이스 관리**: 팀 공간 생성 및 멤버 관리
3. **보드 관리**: 프로젝트 보드 생성 및 설정
4. **카드 관리**: 작업 항목 생성, 수정, 이동
5. **협업 기능**: 댓글, 라벨, 담당자 할당
6. **초대 시스템**: 외부 사용자 초대 및 권한 관리
7. **실시간 협업**: WebSocket 기반 실시간 업데이트

## 📊 도메인 모델 요약

### 핵심 엔티티
```
User (사용자)
├── WorkspaceMember (워크스페이스 멤버)
│   └── Workspace (워크스페이스)
│       └── Board (보드)
│           ├── List (리스트)
│           │   └── Card (카드)
│           │       ├── Comment (댓글)
│           │       ├── CardMember (담당자)
│           │       ├── CardLabel (카드 라벨)
│           │       └── Attachment (첨부파일)
│           ├── Label (라벨)
│           ├── BoardMember (보드 멤버)
│           ├── ActivityLog (활동 로그)
│           └── Invite (초대)
```

### 권한 모델
```
워크스페이스 레벨:
- 소유권: Workspace.createdBy (생성자 사용자 ID)
- 권한: WorkspaceMember.role
  - ADMIN (관리자): 워크스페이스 설정, 멤버 관리, 모든 보드 관리
  - MEMBER (멤버): 보드 생성, 카드 관리
- 생성자 특별 권한: 워크스페이스 삭제, 생성자 이전

보드 레벨:
- ADMIN: 보드 설정, 멤버 관리
- MEMBER: 카드 편집, 댓글 작성
- VIEWER: 읽기 전용 (초대받은 게스트)
```

## 🔧 기술 스택

### 프론트엔드
- **Framework**: React 18+ + TypeScript
- **Build Tool**: Vite
- **State Management**: Zustand
- **Form Validation**: Zod
- **UI Library**: shadcn/ui
- **HTTP Client**: Axios 또는 React Query
- **Routing**: React Router
- **Internationalization**: react-i18next (한국어/영어 지원)
- **Testing**: Vitest, React Testing Library

### 백엔드
- **Runtime**: Java 21 + Spring Boot 3.x
- **Build Tool**: Gradle
- **Architecture**: Hexagonal Architecture (Ports & Adapters)
- **Project Structure**: Multi-module
- **Database**: 
  - Development: H2 Database
  - Production: PostgreSQL
  - Migration: Flyway
  - ORM: Spring Data JPA + QueryDSL (복잡한 쿼리용)
- **Caching**: Redis (추후 적용)
- **File Storage**: 
  - Local Storage (개발/테스트)
  - AWS S3 (운영, 설정 가능)
- **Authentication**: OAuth2 + PKCE, JWT
- **Real-time**: WebSocket (Spring WebSocket)
- **Documentation**: OpenAPI 3.0 (SpringDoc OpenAPI)
- **Testing**: JUnit 5, Mockito, TestContainers

### 인프라
- **Cloud Platform**: AWS (EC2, RDS, S3, CloudWatch)
- **Deployment**: Docker + AWS ECS 또는 EC2
- **CI/CD**: GitHub Actions
- **Monitoring**: 
  - Phase 1: Spring Logback + Console + Sentry + Winston
  - Phase 2: Sentry + CloudWatch + 간단한 대시보드
  - Phase 3: Sentry + CloudWatch + 자체 분석 대시보드 + 실시간 알림

## 📐 설계 원칙

### API 설계
1. **RESTful**: 표준 HTTP 메서드와 상태 코드 사용
2. **일관성**: 모든 API 응답 형식 통일
3. **보안**: 모든 엔드포인트에 권한 검증 적용
4. **문서화**: OpenAPI 스펙으로 자동 문서화

### 데이터 모델링
1. **정규화**: 데이터 중복 최소화
2. **무결성**: 외래키 제약조건 활용
3. **확장성**: 향후 기능 추가를 고려한 유연한 구조
4. **성능**: 적절한 인덱스 설계

### 사용자 경험
1. **직관성**: 쉽게 이해할 수 있는 인터페이스
2. **반응성**: 빠른 응답과 실시간 업데이트
3. **접근성**: 다양한 디바이스와 브라우저 지원
4. **오류 처리**: 명확한 오류 메시지와 복구 방법 제시

## 🚀 개발 프로세스

### 문서 우선 개발
1. **요구사항 분석**: 사용자 스토리 작성
2. **도메인 모델링**: 엔티티와 관계 정의
3. **API 설계**: OpenAPI 스펙 작성
4. **UI/UX 설계**: 와이어프레임과 프로토타입
5. **구현**: 백엔드와 프론트엔드 병렬 개발

### 품질 보증
1. **코드 리뷰**: PR 기반 코드 검토
2. **자동 테스트**: 단위/통합/E2E 테스트
3. **문서 동기화**: 코드와 문서의 일치성 유지
4. **성능 테스트**: 부하 테스트와 최적화

## 📋 주요 기능별 우선순위

### Phase 1: 핵심 기능
- [ ] 사용자 인증 (OAuth2)
- [ ] 워크스페이스 관리 (최대 5개 제한)
- [ ] 보드 및 리스트 관리 (워크스페이스당 최대 20개 제한)
- [ ] 카드 생성, 수정, 이동
- [ ] 기본 권한 관리
- [ ] 파일 첨부 (10MB-50MB 제한)
- [ ] 활동 로그 (90일-180일 보관)

### Phase 2: 협업 기능
- [ ] 댓글 시스템
- [ ] 담당자 할당
- [ ] 라벨 관리
- [ ] 실시간 업데이트
- [ ] 외부 사용자 초대

### Phase 3: 고도화
- [ ] 체크리스트
- [ ] 파일 첨부
- [ ] 알림 시스템
- [ ] 활동 로그
- [ ] 대시보드 및 통계

### Phase 4: 확장 기능
- [ ] Redis 캐싱 구현
- [ ] 고급 모니터링 및 알림
- [ ] API 연동
- [ ] 자동화 (Zapier 등)
- [ ] 고급 권한 관리
- [ ] 백업 및 복구
- [ ] 성능 최적화

## 📊 현재 진행상황

### ✅ 완료된 작업

#### 📋 **프로젝트 기획 및 설계 (100%)**
- [x] **도메인 설계**: User, Workspace, Board, Card 등 핵심 도메인 모델 정의
- [x] **권한 정책**: 소유권(ownerId) vs 권한(ADMIN/MEMBER) 분리 설계
- [x] **사용자 스토리**: 보드 관리 중심 핵심 시나리오 작성
- [x] **비즈니스 규칙**: 워크스페이스 5개, 보드 20개, 첨부파일 제한 등 정의

#### 📡 **API 설계 (100%)**
- [x] **OpenAPI 3.0** 기반 REST API 설계 문서 작성
- [x] **엔드포인트 정의**: 인증, 워크스페이스, 보드, 카드 등 전체 API
- [x] **스키마 표준화**: 공통 스키마, 파라미터, 응답 구조 정의
- [x] **문서 자동화**: API 문서 생성 스크립트 및 프로세스 구축

#### 🛠 **기술 스택 확정 (100%)**
- [x] **Backend**: Spring Boot 3.x + Java 21 + Gradle + Hexagonal Architecture
- [x] **Database**: H2(dev) + PostgreSQL(prod) + Flyway + JPA + QueryDSL
- [x] **Frontend**: React 18+ + TypeScript + Vite + Zustand + Zod + shadcn/ui
- [x] **Infrastructure**: AWS(EC2, RDS, S3, CloudWatch) + Docker
- [x] **Real-time**: WebSocket, **Storage**: Local/S3, **Cache**: Redis(추후)
- [x] **Monitoring**: 단계별 로깅 전략 (Sentry + CloudWatch)
- [x] **i18n**: react-i18next로 한국어/영어 지원

#### 📚 **개발 환경 및 문서화 (100%)**
- [x] **Cursor 컨텍스트**: .cursorrules 상세 작성 (407줄)
- [x] **프로젝트 문서**: PROJECT_CONTEXT.md 종합 가이드 작성
- [x] **아키텍처 구조**: Backend 멀티모듈, Frontend 디렉토리 구조 정의
- [x] **개발 가이드라인**: 코딩 컨벤션, 테스트 전략, CI/CD 방향성

### 🔄 현재 작업 중
**✨ 설계 단계 완료! 이제 실제 개발 시작 준비가 되었습니다.**

### 📋 다음 단계 (우선순위 순)

#### 🚀 **Phase 1: 프로젝트 설정 (1-2주) - 즉시 시작 가능!**
- [ ] **Backend 프로젝트 초기 설정**
  - [ ] Spring Boot 3.x + Java 21 + Gradle 프로젝트 생성
  - [ ] Hexagonal Architecture 기반 멀티 모듈 구조 설정
  - [ ] H2 Database 설정 (개발용)
  - [ ] PostgreSQL 설정 (운영용)
  - [ ] Flyway 데이터베이스 마이그레이션 설정
  - [ ] Spring Data JPA 설정
  - [ ] QueryDSL 설정 (복잡한 쿼리용)
  - [ ] SpringDoc OpenAPI 설정
  - [ ] OAuth2 + JWT 인증 설정
  - [ ] 파일 저장소 설정 (로컬/S3)
  - [ ] WebSocket 설정
  - [ ] 기본 도메인 모델 구현 (User, Workspace, Board)
  - [ ] 비즈니스 규칙 구현 (워크스페이스 5개, 보드 20개 제한)

- [ ] **Frontend 프로젝트 초기 설정**
  - [ ] React + TypeScript + Vite 프로젝트 생성
  - [ ] Zustand 상태 관리 설정
  - [ ] Zod 폼 검증 설정
  - [ ] shadcn/ui 설정
  - [ ] API 클라이언트 설정
  - [ ] 기본 라우팅 설정
  - [ ] react-i18next 국제화 설정 (한국어/영어)

#### Phase 2: 핵심 기능 구현 (3-4주)
- [ ] **Backend 핵심 기능**
  - [ ] 사용자 인증 및 권한 관리
  - [ ] 워크스페이스 CRUD
  - [ ] 보드 CRUD
  - [ ] 리스트 및 카드 CRUD
  - [ ] 권한 기반 접근 제어

- [ ] **Frontend 핵심 기능**
  - [ ] 로그인/회원가입 페이지
  - [ ] 워크스페이스 관리 페이지
  - [ ] 보드 생성 및 관리
  - [ ] 칸반 보드 UI (드래그 앤 드롭)
  - [ ] 카드 생성 및 편집

#### Phase 3: 협업 기능 (5-6주)
- [ ] **Backend 협업 기능**
  - [ ] 댓글 시스템
  - [ ] 라벨 관리
  - [ ] 담당자 할당
  - [ ] 외부 사용자 초대
  - [ ] 실시간 업데이트 (WebSocket)

- [ ] **Frontend 협업 기능**
  - [ ] 댓글 UI
  - [ ] 라벨 관리 UI
  - [ ] 담당자 할당 UI
  - [ ] 초대 링크 생성 및 공유
  - [ ] 실시간 업데이트 UI

#### Phase 4: 고도화 (7-8주)
- [ ] **성능 최적화**
  - [ ] 데이터베이스 쿼리 최적화
  - [ ] 프론트엔드 번들 최적화
  - [ ] 캐싱 전략
  - [ ] 이미지 최적화

- [ ] **테스트 및 품질**
  - [ ] 단위 테스트 (JUnit 5, Vitest)
  - [ ] 통합 테스트
  - [ ] E2E 테스트
  - [ ] 성능 테스트

- [ ] **배포 및 운영**
  - [ ] Docker 컨테이너화
  - [ ] CI/CD 파이프라인
  - [ ] 모니터링 및 로깅
  - [ ] 백업 및 복구

## 🎯 **바로 다음에 할 일 - Phase 1 시작!**

### **1. Backend 프로젝트 생성 (우선순위 1)**
```bash
# 1. backend/ 폴더 생성
mkdir backend
cd backend

# 2. Spring Initializr로 프로젝트 생성
curl https://start.spring.io/starter.zip \
  -d type=gradle-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d baseDir=boardly-backend \
  -d groupId=com.boardly \
  -d artifactId=boardly-backend \
  -d packageName=com.boardly \
  -d packaging=jar \
  -d javaVersion=21 \
  -d dependencies=web,data-jpa,h2,postgresql,validation,actuator \
  -o boardly-backend.zip

# 3. 압축 해제 및 폴더명 변경
unzip boardly-backend.zip
mv boardly-backend/* .
rmdir boardly-backend
rm boardly-backend.zip

# 4. 멀티모듈 구조로 변경
# 5. Hexagonal Architecture 패키지 구조 설정
# 6. Flyway, QueryDSL, SpringDoc 의존성 추가
```

### **2. Frontend 프로젝트 생성 (우선순위 2)**
```bash
# 1. frontend/ 폴더 생성
mkdir frontend
cd frontend

# 2. Vite + React + TypeScript 프로젝트 생성
npm create vite@latest . -- --template react-ts

# 3. 핵심 의존성 설치
npm install zustand zod @hookform/react-hook-form
npm install @radix-ui/react-* class-variance-authority clsx tailwind-merge
npm install react-i18next i18next axios
npm install @dnd-kit/core @dnd-kit/sortable @dnd-kit/utilities

# 4. 개발 의존성 설치
npm install -D @types/node tailwindcss autoprefixer postcss
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

### **3. 첫 번째 마일스톤 목표**
- [ ] Backend: 프로젝트 구조 + H2 + 기본 User 엔티티
- [ ] Frontend: 프로젝트 구조 + shadcn/ui + 기본 라우팅
- [ ] 통합: Hello World API 연결 테스트

### **4. 예상 소요 시간**
- **Backend 설정**: 2-3일
- **Frontend 설정**: 2-3일  
- **통합 테스트**: 1일
- **총 예상**: **1주일**

## 🔍 핵심 사용 시나리오

### 시나리오 1: 개인 사용자
1. 개인 워크스페이스에서 프로젝트 관리
2. 할 일, 진행중, 완료 리스트로 작업 추적
3. 마감일과 라벨로 우선순위 관리

### 시나리오 2: 소규모 팀
1. 팀 워크스페이스 생성 및 멤버 초대
2. 프로젝트별 보드 생성
3. 팀원 간 작업 분배 및 협업
4. 댓글로 소통 및 피드백

### 시나리오 3: 외부 협업
1. 외부 클라이언트나 파트너 초대
2. 특정 보드에만 접근 권한 부여
3. 제한된 권한으로 안전한 협업
4. 프로젝트 진행 상황 공유

## 🛡️ 보안 고려사항

### 인증 및 인가
- OAuth2 PKCE 플로우로 안전한 인증
- JWT 토큰의 적절한 만료 시간 설정
- 세분화된 권한 체계로 최소 권한 원칙 적용

### 데이터 보호
- HTTPS 강제 사용
- 입력 데이터 검증 및 sanitization
- SQL Injection, XSS 공격 방지
- 민감 정보 암호화 저장

### API 보안
- Rate Limiting으로 남용 방지
- CORS 정책 적용
- API 키 관리 (필요시)
- 감사 로그 기록

## 📈 성능 고려사항

### 백엔드 최적화
- 데이터베이스 쿼리 최적화
- 적절한 캐싱 전략
- 페이지네이션으로 대용량 데이터 처리
- 비동기 처리로 응답성 향상

### 프론트엔드 최적화
- 코드 스플리팅으로 초기 로딩 시간 단축
- 이미지 최적화 및 lazy loading
- 메모이제이션으로 불필요한 리렌더링 방지
- Service Worker로 오프라인 지원

### 실시간 기능
- WebSocket 연결 최적화
- 효율적인 이벤트 전파
- 연결 끊김 처리 및 재연결
- 대용량 동시 접속 지원

## 🔄 확장성 고려사항

### 수평 확장
- 마이크로서비스 아키텍처 고려
- 데이터베이스 샤딩 전략
- 로드 밸런싱
- CDN 활용

### 기능 확장
- 플러그인 시스템 설계
- API 공개로 서드파티 연동
- 웹훅으로 외부 시스템 연동
- 다국어 지원 준비

이 컨텍스트 문서를 바탕으로 일관성 있는 설계 문서를 작성하고, 프로젝트의 전체적인 방향성을 유지하면서 개발을 진행할 수 있습니다.
