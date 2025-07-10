# Boardly Frontend

React + TypeScript + Vite 기반의 프론트엔드 애플리케이션입니다.

## OAuth 인증 설정

이 프로젝트는 `react-oidc-context`를 사용하여 OAuth 인증을 구현합니다.

### 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 환경 변수들을 설정하세요:

```env
# API URL
VITE_API_URL=http://localhost:3000

# OAuth Configuration
VITE_OAUTH_AUTHORIZATION_ENDPOINT=https://your-oauth-provider.com/oauth/authorize
VITE_OAUTH_CLIENT_ID=your-client-id
VITE_OAUTH_CLIENT_SECRET=your-client-secret
VITE_OAUTH_RESPONSE_TYPE=code
VITE_OAUTH_REDIRECT_URI=http://localhost:5173/callback
VITE_OAUTH_POST_LOGOUT_REDIRECT_URI=http://localhost:5173
VITE_OAUTH_SCOPE=openid profile email
VITE_OAUTH_CLIENT_AUTHENTICATION=client_secret_basic
```

### 주요 기능

- **OAuth 인증**: react-oidc-context를 사용한 OAuth 2.0 인증
- **보호된 라우트**: 인증이 필요한 페이지 보호
- **사용자 정보 표시**: 로그인한 사용자 정보 표시
- **자동 토큰 갱신**: 액세스 토큰 자동 갱신

### 사용법

1. **로그인**: `useOAuth` 훅의 `login()` 함수 호출
2. **로그아웃**: `useOAuth` 훅의 `logout()` 함수 호출
3. **보호된 라우트**: `ProtectedRoute` 컴포넌트로 감싸기
4. **인증 상태 확인**: `useOAuth` 훅의 `isAuthenticated` 속성 사용

## 개발 환경 설정

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default tseslint.config([
  globalIgnores(["dist"]),
  {
    files: ["**/*.{ts,tsx}"],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      ...tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      ...tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      ...tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ["./tsconfig.node.json", "./tsconfig.app.json"],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
]);
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from "eslint-plugin-react-x";
import reactDom from "eslint-plugin-react-dom";

export default tseslint.config([
  globalIgnores(["dist"]),
  {
    files: ["**/*.{ts,tsx}"],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs["recommended-typescript"],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ["./tsconfig.node.json", "./tsconfig.app.json"],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
]);
```
