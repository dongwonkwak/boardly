# Boardly frontend architecture

## 개요

Boardly는 React와 TypeScript로 구축된 칸반 보드 애플리케이션입니다. OpenAPI 스펙을 기반으로 백엔드 API와 통신하며, 사용자 인증 후 대시보드에서 칸반 보드를 관리할 수 있습니다.

## 기술 스택

- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Package Manager**: pnpm
- **UI Library**: shadcn/ui
- **Styling**: Tailwind CSS
- **State Management**: React Context API 또는 Zustand
- **Routing**: React Router DOM
- **API Client**: OpenAPI Generator로 생성된 클라이언트
- **Form Handling**: React Hook Form
- **Validation**: Zod
- **Internationalization**: react-i18next

## 폴더 구조

```
src/
├── components/           # 재사용 가능한 UI 컴포넌트
│   ├── ui/              # shadcn/ui 컴포넌트
│   ├── common/          # 커스텀 공통 컴포넌트
│   ├── layout/          # 레이아웃 관련 컴포넌트
│   └── kanban/          # 칸반 관련 컴포넌트
├── pages/               # 페이지 컴포넌트
│   ├── Landing/         # 랜딩 페이지
│   ├── Auth/            # 인증 관련 페이지
│   ├── Dashboard/       # 대시보드
│   ├── Board/           # 칸반 보드 페이지
│   └── Settings/        # 설정 페이지
├── hooks/               # 커스텀 훅
├── services/            # API 서비스 및 외부 통신
│   └── api/             # OpenAPI로 생성된 클라이언트
├── stores/              # 상태 관리
├── types/               # TypeScript 타입 정의
├── utils/               # 유틸리티 함수
├── styles/              # 전역 스타일
│   ├── globals.css      # Tailwind CSS 설정
│   └── themes/          # 다크/라이트 테마 설정
├── assets/              # 정적 자산
│   ├── images/
│   └── locales/         # 다국어 리소스
│       ├── en/
│       └── ko/
├── config/              # 설정 파일
├── lib/                 # 유틸리티 라이브러리
│   └── utils.ts         # shadcn/ui 유틸리티
└── App.tsx              # 메인 앱 컴포넌트
```

## 주요 기능 및 페이지

### 1. 랜딩 페이지 (`/`)

- 앱 소개 및 주요 기능 설명
- 회원가입 버튼 및 로그인 버튼
- 반응형 디자인

### 2. 인증 페이지

- **회원가입** (`/register`): 새 계정 생성
- **비밀번호 찾기** (`/forgot-password`): 비밀번호 재설정
- **OAuth2 로그인**: Authorization Code Flow with PKCE 방식으로 백엔드 OAuth 서버와 통신

### 3. 대시보드 (`/dashboard`)

- 사용자의 모든 보드 목록
- 새 보드 생성 기능
- 보드 검색 및 필터링
- 최근 활동 내역

### 4. 칸반 보드 (`/board/:id`)

- 드래그 앤 드롭으로 카드 이동
- 컬럼 추가/삭제/편집
- 카드 추가/삭제/편집
- 실시간 협업 기능

## 상태 관리 전략

### 1. 전역 상태

- **AuthStore**: 사용자 인증 상태, 토큰 관리
- **BoardStore**: 보드 데이터, 실시간 업데이트
- **UIStore**: 모달, 사이드바, 테마 등 UI 상태
- **LanguageStore**: 언어 설정, 다국어 리소스 관리

### 2. 로컬 상태

- 폼 데이터, 임시 편집 상태 등은 컴포넌트 내부에서 관리

## API 통신

### 1. OpenAPI 클라이언트

- 백엔드에서 제공하는 OpenAPI 스펙을 기반으로 클라이언트 자동 생성
- 타입 안전성 보장
- API 응답/요청 타입 자동 생성

### 2. 에러 처리

- 전역 에러 핸들링
- 네트워크 오류, 인증 오류 등 상황별 처리
- 사용자 친화적인 에러 메시지

## 라우팅 구조

```typescript
/                    # 랜딩 페이지
/register           # 회원가입
/forgot-password    # 비밀번호 찾기
/callback           # OAuth2 콜백 처리
/dashboard          # 대시보드 (인증 필요)
/board/:id          # 칸반 보드 (인증 필요)
/settings           # 설정 (인증 필요)
```

## 보안 고려사항

1. **OAuth2 PKCE 플로우**: Authorization Code Flow with PKCE로 안전한 인증
2. **토큰 관리**: Access Token과 Refresh Token을 안전하게 저장하고 관리
3. **라우트 보호**: 인증이 필요한 페이지에 대한 접근 제어
4. **API 요청 보안**: 모든 API 요청에 Authorization 헤더 포함
5. **XSS 방지**: 사용자 입력 데이터 검증 및 이스케이프
6. **PKCE 보안**: Code Verifier를 안전하게 생성하고 검증

## UI/UX 설계

### 1. shadcn/ui 활용

- **컴포넌트 기반**: 재사용 가능한 UI 컴포넌트 라이브러리
- **Tailwind CSS**: 유틸리티 퍼스트 CSS 프레임워크
- **다크/라이트 테마**: 사용자 선호도에 따른 테마 전환
- **접근성**: WCAG 가이드라인 준수

### 2. 디자인 시스템

- **일관된 디자인**: shadcn/ui의 디자인 토큰 활용
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 지원
- **애니메이션**: Framer Motion을 활용한 부드러운 전환 효과
- **로딩 상태**: 스켈레톤 UI 및 로딩 스피너

### 3. 칸반 보드 UI

- **드래그 앤 드롭**: react-beautiful-dnd 또는 @dnd-kit/core 활용
- **실시간 업데이트**: 실시간 협업을 위한 UI 피드백
- **컨텍스트 메뉴**: 우클릭 메뉴로 빠른 액션 제공
- **키보드 단축키**: 접근성을 위한 키보드 네비게이션

## 성능 최적화

1. **코드 스플리팅**: 페이지별 lazy loading
2. **메모이제이션**: React.memo, useMemo, useCallback 활용
3. **이미지 최적화**: WebP 포맷 사용, lazy loading
4. **번들 최적화**: Tree shaking, 불필요한 의존성 제거
5. **CSS 최적화**: Tailwind CSS의 JIT 컴파일러 활용

## 다국어 지원 (i18n)

### 1. 지원 언어

- **영어 (en)**: 기본 언어
- **한국어 (ko)**: 한국 사용자 지원

### 2. 구현 방식

- **react-i18next**: React용 i18n 라이브러리
- **언어 감지**: 브라우저 언어 설정 자동 감지
- **언어 전환**: 헤더의 LanguageSelector 컴포넌트
- **지속성**: 로컬 스토리지를 통한 언어 설정 저장

### 3. 번역 파일 구조

```
locales/
├── en/
│   ├── common.json      # 공통 텍스트 (버튼, 메시지 등)
│   ├── auth.json        # 인증 관련 텍스트
│   ├── dashboard.json   # 대시보드 관련 텍스트
│   └── board.json       # 칸반 보드 관련 텍스트
└── ko/
    ├── common.json
    ├── auth.json
    ├── dashboard.json
    └── board.json
```

### 4. 사용 예시

```typescript
// 컴포넌트에서 사용
import { useTranslation } from "react-i18next";

const MyComponent = () => {
  const { t } = useTranslation("common");

  return <button>{t("buttons.save")}</button>;
};
```

## OAuth2 PKCE 플로우

### 1. 인증 과정

1. **로그인 시작**: 사용자가 OAuth2 로그인 버튼 클릭
2. **PKCE 생성**: Code Verifier와 Code Challenge 생성
3. **인증 요청**: 백엔드 OAuth 서버로 Authorization Code 요청
4. **사용자 인증**: 백엔드에서 사용자 로그인 처리
5. **콜백 처리**: Authorization Code와 함께 프론트엔드로 리다이렉트
6. **토큰 교환**: Authorization Code와 Code Verifier로 Access Token 교환
7. **세션 시작**: Access Token으로 사용자 세션 시작

### 2. 구현 파일

- **`config/`**: OAuth2 설정 (client_id, redirect_uri, scope 등)
- **`services/`**: OAuth2 PKCE 플로우 처리 로직
- **`hooks/`**: OAuth2 관련 커스텀 훅
- **`pages/Callback/`**: OAuth2 콜백 처리 페이지

### 3. 보안 특징

- **PKCE**: Authorization Code Interception 공격 방지
- **State 파라미터**: CSRF 공격 방지
- **토큰 저장**: HttpOnly 쿠키 또는 안전한 로컬 스토리지
- **토큰 갱신**: Refresh Token을 통한 자동 토큰 갱신

## 개발 환경 설정

1. **ESLint**: 코드 품질 및 일관성 유지
2. **Prettier**: 코드 포맷팅
3. **Husky**: Git hooks를 통한 코드 품질 관리
4. **TypeScript**: 타입 안전성 보장
