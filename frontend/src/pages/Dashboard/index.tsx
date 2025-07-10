import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useOAuth } from "@/hooks/useAuth";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function Dashboard() {
  const { user } = useOAuth();

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
              <div className="space-y-2">
                <p><strong>이름:</strong> {user?.profile.name || "N/A"}</p>
                <p><strong>이메일:</strong> {user?.profile.email || "N/A"}</p>
                <p><strong>ID:</strong> {user?.profile.sub || "N/A"}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>인증 정보</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <p><strong>토큰 만료:</strong> {user?.expires_at ? new Date(user.expires_at * 1000).toLocaleString() : "N/A"}</p>
                <p><strong>스코프:</strong> {user?.scope || "N/A"}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>액세스 토큰</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-gray-600 break-all">
                {user?.access_token ? `${user.access_token.substring(0, 50)}...` : "N/A"}
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  );
} 