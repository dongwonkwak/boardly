import { useOAuth } from "@/hooks/useAuth";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import type { ReactNode } from "react";

interface ProtectedRouteProps {
  children: ReactNode;
  fallback?: ReactNode;
}

export function ProtectedRoute({ children, fallback }: ProtectedRouteProps) {
  const { t } = useTranslation("common");
  const { isAuthenticated, isLoading } = useOAuth();
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate("/");
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
        <span className="ml-2">{t("auth.checking")}</span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      fallback || (
        <div className="flex flex-col items-center justify-center min-h-screen">
          <h2 className="text-2xl font-bold mb-4">{t("auth.login_required")}</h2>
          <p className="text-gray-600 mb-6">{t("auth.login_required_desc")}</p>
          <Button onClick={handleGoHome} variant="default">
            홈으로 가기
          </Button>
        </div>
      )
    );
  }

  return <>{children}</>;
} 