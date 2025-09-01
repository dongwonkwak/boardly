import { useEffect } from "react";
import { useOAuth } from "@/hooks/useAuth";
import { useTranslation } from "react-i18next";

/**
 * OAuth 콜백을 처리하는 컴포넌트
 */
export default function Callback() {
  const { auth } = useOAuth();
  const { t } = useTranslation("common");
  
  useEffect(() => {
    // 인증 완료 후 대시보드로 리다이렉트
    if (auth.isAuthenticated) {
      window.location.href = "/dashboard";
    }
  }, [auth.isAuthenticated]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
      <span className="ml-2">{t("auth.checking")}</span>
    </div>
  );
}