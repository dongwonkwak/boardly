import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useOAuth } from "@/hooks/useAuth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useEffect } from "react";

export default function Dashboard() {
  const { user: oauthUser, isLoading } = useOAuth();

  useEffect(() => {
    console.log("OIDC User Info:", oauthUser);
  }, [oauthUser]);

  return (
    <ProtectedRoute>
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold mb-8">대시보드</h1>
        
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader>
              <CardTitle>사용자 정보</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading || !oauthUser ? (
                <p className="text-gray-500">로딩 중...</p>
              ) : oauthUser.profile ? (
                <div className="space-y-2">
                  <p><strong>이름:</strong> {
                    oauthUser.profile.name || 
                    `${oauthUser.profile.given_name || ""} ${oauthUser.profile.family_name || ""}`.trim() || 
                    "N/A"
                  }</p>
                  <p><strong>이메일:</strong> {oauthUser.profile.email || "N/A"}</p>
                  <p><strong>사용자 ID:</strong> {oauthUser.profile.sub || "N/A"}</p>
                  {oauthUser.profile.preferred_username && (
                    <p><strong>사용자명:</strong> {oauthUser.profile.preferred_username}</p>
                  )}
                  {oauthUser.profile.email_verified !== undefined && (
                    <p><strong>이메일 인증:</strong> {oauthUser.profile.email_verified ? "인증됨" : "미인증"}</p>
                  )}
                  {oauthUser.profile.locale && (
                    <p><strong>언어:</strong> {oauthUser.profile.locale}</p>
                  )}
                </div>
              ) : (
                <p className="text-gray-500">사용자 정보를 불러올 수 없습니다.</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>인증 정보</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <p><strong>토큰 만료:</strong> {oauthUser?.expires_at ? new Date(oauthUser.expires_at * 1000).toLocaleString() : "N/A"}</p>
                <p><strong>스코프:</strong> {oauthUser?.scope || "N/A"}</p>
                <p><strong>토큰 타입:</strong> {oauthUser?.token_type || "N/A"}</p>
                {oauthUser?.session_state && (
                  <p><strong>세션 상태:</strong> {oauthUser.session_state}</p>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>액세스 토큰</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-600 break-all">
                {oauthUser?.access_token ? `${oauthUser.access_token.substring(0, 50)}...` : "N/A"}
              </p>
              {oauthUser?.id_token && (
                <div className="mt-3">
                  <p className="text-sm font-medium text-gray-700 mb-1">ID 토큰:</p>
                  <p className="text-sm text-gray-600 break-all">
                    {oauthUser.id_token.substring(0, 50)}...
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  );
} 