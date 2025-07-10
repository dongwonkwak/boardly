import { useEffect } from "react";
import { useOAuth } from "@/hooks/useAuth";

/**
 * OAuth 콜백을 처리하는 컴포넌트
 */
export default function Callback() {
  const { auth } = useOAuth();

  useEffect(() => {
    // 인증 콜백 처리
    if (auth.isAuthenticated) {
      window.location.href = "/dashboard";
    }
  }, [auth.isAuthenticated]);

  return null;
}