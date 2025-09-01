import { AuthProvider } from "react-oidc-context";
import { useEffect } from "react";
import env from "@/env";
import { useOAuth } from "@/hooks/useAuth";
import type { ReactNode } from "react";

interface OAuthProviderProps {
  children: ReactNode;
}

export function OAuthProvider({ children }: OAuthProviderProps) {

  const oauthConfig = {
    authority: env.VITE_OAUTH_AUTHORIZATION_ENDPOINT,
    client_id: env.VITE_OAUTH_CLIENT_ID,
    client_secret: env.VITE_OAUTH_CLIENT_SECRET,
    redirect_uri: env.VITE_OAUTH_REDIRECT_URI,
    post_logout_redirect_uri: env.VITE_OAUTH_POST_LOGOUT_REDIRECT_URI,
    response_type: env.VITE_OAUTH_RESPONSE_TYPE,
    scope: env.VITE_OAUTH_SCOPE,
    client_authentication: env.VITE_OAUTH_CLIENT_AUTHENTICATION,
    loadUserInfo: true,
    monitorSession: true,
    automaticSilentRenew: true,
    silent_redirect_uri: env.VITE_OAUTH_REDIRECT_URI,
    onSigninCallback: () => {
      // 로그인 성공 시 홈으로 이동
      window.location.href = "/";
    },
  };

  return (
    <AuthProvider {...oauthConfig}>
      <AuthRedirectHandler />
      {children}
    </AuthProvider>
  );
}

// 인증 상태에 따른 리디렉션 처리
function AuthRedirectHandler() {
  
  const { isAuthenticated, isLoading } = useOAuth();
  const currentPath = window.location.pathname;

  useEffect(() => {
    // 로딩 중이 아니고 인증된 상태이며, 홈 페이지에 있다면 대시보드로 리디렉션
    if (!isLoading && isAuthenticated && currentPath === "/") {
      window.location.href = "/dashboard";
    }
  }, [isAuthenticated, isLoading, currentPath]);

  return null;
}
