import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useOAuth } from "@/hooks/useAuth";
import { useUser, useUserLoading, useUserError } from "@/store/userStore";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function Dashboard() {
  const { user: oauthUser } = useOAuth();
  const user = useUser();
  const isLoading = useUserLoading();
  const error = useUserError();


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
              {isLoading ? (
                <p className="text-gray-500">로딩 중...</p>
              ) : error ? (
                <p className="text-red-500">오류: {error}</p>
              ) : user ? (
                <div className="space-y-2">
                  <p><strong>이름:</strong> {user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : "N/A"}</p>
                  <p><strong>이메일:</strong> {user.email || "N/A"}</p>
                  <p><strong>사용자 ID:</strong> {user.userId || "N/A"}</p>
                  <p><strong>활성 상태:</strong> {user.isActive ? "활성" : "비활성"}</p>
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
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  );
} 