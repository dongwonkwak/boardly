# 테스팅 가이드

이 프로젝트는 **Vitest**와 **React Testing Library**를 사용하여 단위 테스트를 구현합니다.

## 🛠️ 설치된 테스트 라이브러리

- **vitest**: 빠른 테스트 실행 환경
- **@testing-library/react**: React 컴포넌트 테스팅
- **@testing-library/jest-dom**: DOM 매처 확장
- **@testing-library/user-event**: 사용자 상호작용 시뮬레이션
- **jsdom**: 브라우저 환경 시뮬레이션
- **@vitest/coverage-v8**: 코드 커버리지

## 📝 사용 가능한 명령어

```bash
# 테스트 실행 (watch 모드)
pnpm test

# 단일 실행
pnpm test:run

# 커버리지 포함 실행
pnpm test:coverage
```

## 📁 테스트 파일 구조

```
src/
├── lib/
│   └── utils.test.ts              # 유틸리티 함수 테스트
├── utils/
│   └── logger.test.ts             # 로거 설정 테스트
├── hooks/
│   └── useAuth.test.tsx           # 커스텀 훅 테스트
├── components/
│   ├── ui/                        # 🚫 테스트 제외 폴더
│   ├── common/
│   │   ├── LoadingButton.test.tsx # LoadingButton 테스트
│   │   └── SuccessModal.test.tsx  # Modal 컴포넌트 테스트
│   └── layout/
│       └── Navbar.test.tsx        # 네비게이션 테스트
└── test-setup.ts                  # 테스트 환경 설정
```

## 🧪 테스트 구성

### 1. 유틸리티 함수 테스트

- `cn()` 함수의 클래스명 병합 기능
- 조건부 클래스, 충돌 클래스 처리
- Tailwind CSS 클래스 우선순위

### 2. UI 컴포넌트 테스트

- **LoadingButton**: 로딩 상태, 스피너 표시
- **SuccessModal**: 모달 렌더링, 키보드 이벤트, 접근성
- ⚠️ **ui 폴더**: 테스트에서 제외됨 (Button, Card, Input 등)

### 3. 레이아웃 컴포넌트 테스트

- **Navbar**: 네비게이션 링크, 버튼 상호작용, 반응형

### 4. 훅 테스트

- **useOAuth**: 인증 상태, 로그인/로그아웃 기능

## 🎯 테스트 작성 가이드

### 컴포넌트 테스트 예시

```typescript
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Button } from "./button";

describe("Button Component", () => {
  it("should render with default props", () => {
    render(<Button>Click me</Button>);
    const button = screen.getByRole("button", { name: "Click me" });
    expect(button).toBeInTheDocument();
  });

  it("should handle click events", async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();

    render(<Button onClick={handleClick}>Click me</Button>);
    const button = screen.getByRole("button");

    await user.click(button);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });
});
```

### 유틸리티 함수 테스트 예시

```typescript
import { describe, it, expect } from "vitest";
import { cn } from "./utils";

describe("cn utility function", () => {
  it("should merge class names correctly", () => {
    const result = cn("text-red-500", "bg-blue-500");
    expect(result).toBe("text-red-500 bg-blue-500");
  });
});
```

## 🎨 테스트 모범 사례

### 1. 테스트 네이밍

- 무엇을 테스트하는지 명확하게 설명
- `should + 동작 + 조건` 패턴 사용

### 2. 접근성 중심 쿼리 사용

```typescript
// ✅ 좋은 예
screen.getByRole("button", { name: "Submit" });
screen.getByLabelText("Email address");

// ❌ 피해야 할 예
screen.getByTestId("submit-button");
```

### 3. 사용자 중심 테스트

```typescript
const user = userEvent.setup();
await user.click(button);
await user.type(input, "test value");
```

### 4. Mock 사용

```typescript
// 외부 의존성 모킹
vi.mock("react-oidc-context", () => ({
  useAuth: () => mockAuth,
}));

// 함수 모킹
const handleClick = vi.fn();
```

## 📊 현재 테스트 현황

- **총 테스트 파일**: 6개
- **총 테스트 케이스**: 47개
- **통과율**: 100% ✅
- **제외된 폴더**: `src/components/ui` (shadcn/ui 컴포넌트)

## 🔧 테스트 설정 파일

### `vitest.config.ts`

- Vitest 환경 설정
- jsdom 환경 사용
- 테스트 셋업 파일 지정
- `src/components/ui` 폴더 테스트 제외 설정

### `src/test-setup.ts`

- Jest DOM 매처 추가
- 전역 Mock 설정 (matchMedia, ResizeObserver)
- 환경 변수 Mock

## 🚀 새로운 테스트 추가하기

1. 테스트할 파일과 같은 디렉토리에 `.test.ts` 또는 `.test.tsx` 파일 생성
2. 필요한 라이브러리 import
3. `describe` 블록으로 테스트 그룹 구성
4. `it` 블록으로 개별 테스트 케이스 작성
5. `pnpm test` 명령어로 실행

## 📚 참고 자료

- [Vitest 공식 문서](https://vitest.dev/)
- [React Testing Library 가이드](https://testing-library.com/docs/react-testing-library/intro/)
- [Jest DOM 매처](https://github.com/testing-library/jest-dom)
- [User Event 라이브러리](https://testing-library.com/docs/user-event/intro/)
