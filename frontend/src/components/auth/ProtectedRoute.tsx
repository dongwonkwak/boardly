import { useOAuth } from "@/hooks/useAuth";
import { useTranslation } from "react-i18next";
import type { ReactNode } from "react";

interface ProtectedRouteProps {
  children: ReactNode;
  fallback?: ReactNode;
}

export function ProtectedRoute({ children, fallback }: ProtectedRouteProps) {
  const { t } = useTranslation("common");
  const { isAuthenticated, isLoading } = useOAuth();

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
          <p className="text-gray-600 mb-4">{t("auth.login_required_desc")}</p>
        </div>
      )
    );
  }

  return <>{children}</>;
} 