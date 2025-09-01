# Frontend Architecture — Boardly (개정본)

## 기술 스택

* **React 19**: Concurrent Features, Suspense 등 최신 기능 활용
* **TypeScript 5.x**: 정적 타입 안정성
* **Vite 5.x**: 빠른 번들링 및 HMR

---

## 아키텍처 설계

* **도메인별 모듈화 구조**: workspace, board, card 등 기능 단위 폴더 분리
* **컴포넌트 기반 개발**: 재사용 가능한 UI 컴포넌트 구성
* **상태 관리**:

  * **React Query (TanStack Query)**: 서버 상태 관리, OpenAPI 클라이언트와 연동
  * **Context API + useReducer**: 전역 클라이언트 상태 관리
  * **useState**: 로컬 UI 상태

---

## UI 및 UX

* **Ant Design 5.x**: 엔터프라이즈급 UI 컴포넌트 라이브러리
* **@dnd-kit**: 고성능 드래그앤드롭 구현
* **반응형 디자인**: 모바일 친화적 레이아웃
* **다국어(i18n)**: MVP 포함 — 라우트/컴포넌트 단위 번역 키, 날짜/숫자 포맷 정책
* **테마 커스터마이징**: Ant Design Theme Config 기반

---

## API 통신

* **OpenAPI 3.0 기반**: 백엔드 스펙 우선 개발
* **자동 생성 클라이언트**: oazapfts 또는 typescript-fetch 기반 코드 생성
* **React Query 연동**: 생성된 API 함수 → queryFn/mutationFn에 연결
* **에러 처리**: 공통 에러 핸들러(Hook) + 토스트 알림

---

## 실시간 기능

* **SSE (Server-Sent Events)**: 서버→클라이언트 단방향 알림 처리
* **구독 단위**: 워크스페이스/보드 이벤트 스트림
* **클라이언트 구현**: 브라우저 `EventSource` 사용
* **인증**: HTTP-only 쿠키 또는 짧은 만료 토큰을 쿼리파라미터로 전달
* **재연결 전략**: EventSource 기본 재연결 + 커스텀 백오프 로직

---

## 개발 환경

* **Docker Compose**: 로컬 API 서버/DB 연동
* **Biome**: 코드 품질 및 포맷팅
* **Vitest**: 단위/컴포넌트 테스트
* **Playwright**: E2E 테스트(로그인 → 대시보드)
* **Storybook (선택)**: 컴포넌트 개발/문서화

---

## 테스트 전략

* **단위 테스트**: React 컴포넌트, 훅 로직 검증
* **통합 테스트**: React Query + OpenAPI 클라이언트 연동
* **E2E 테스트**: 사용자 플로우 (로그인, 워크스페이스/보드/카드 시나리오)
* **커버리지 목표**: 전체 75% 이상, 핵심 뷰/훅 90% 이상

---

## 성능 최적화

* 코드 스플리팅: 라우트 단위 청크 분리
* React.lazy + Suspense 활용
* 이미지 최적화 (responsive image, lazy loading)
* 캐싱 전략: React Query staleTime + background revalidation

---

## 확장성 고려

* PWA(Progressive Web App) 옵션 검토
* 접근성(Accessibility, a11y) 준수
* 브라우저 호환성(최신 Chrome/Firefox/Edge 우선, Safari 보조)

---

## 기술적 의사결정 요약

* **React 19 + TS 5 + Vite 5**: 최신 프런트엔드 스택, 생산성 + 성능
* **React Query + OpenAPI 클라이언트**: 타입 안전한 서버 상태 관리
* **SSE 기반 실시간**: 단순 구현, 알림/활동 피드 반영
* **Ant Design + dnd-kit**: 빠른 UI 구축과 칸반 UX 최적화
* **현실적 단순화**: Storybook/PWA는 선택, **i18n은 MVP 포함**
