import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useOAuth } from "@/hooks/useAuth";
import { useApi } from "@/hooks/useApi";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import type * as apiClient from "@/services/api/client";

export default function Dashboard() {
  const { user: oauthUser, isLoading } = useOAuth();
  const { authenticated } = useApi();
  const [boards, setBoards] = useState<apiClient.BoardResponse[]>([]);
  const [loadingBoards, setLoadingBoards] = useState(false);

  useEffect(() => {
    console.log("OIDC User Info:", oauthUser);
  }, [oauthUser]);

  // 보드 목록 조회 예시
  const handleLoadBoards = async () => {
    if (!authenticated) {
      console.log("인증되지 않은 상태입니다.");
      return;
    }

    setLoadingBoards(true);
    try {
      const result = await (authenticated.getMyBoards as typeof apiClient.getMyBoards)({ includeArchived: false });
      if (result.status === 200) {
        setBoards(result.data || []);
        console.log("보드 목록:", result.data);
      }
    } catch (error) {
      console.error("보드 목록 조회 실패:", error);
    } finally {
      setLoadingBoards(false);
    }
  };

  // 새 보드 생성 예시
  const handleCreateBoard = async () => {
    if (!authenticated) {
      console.log("인증되지 않은 상태입니다.");
      return;
    }

    try {
      const result = await (authenticated.createBoard as typeof apiClient.createBoard)({
        title: "새 보드",
        description: "새로 생성된 보드입니다."
      });
      if (result.status === 201) {
        console.log("보드 생성 성공:", result.data);
        // 보드 목록 새로고침
        handleLoadBoards();
      }
    } catch (error) {
      console.error("보드 생성 실패:", error);
    }
  };

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

          {/* 새로운 API 클라이언트 사용 예시 */}
          <Card>
            <CardHeader>
              <CardTitle>API 테스트</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <Button 
                  onClick={handleLoadBoards} 
                  disabled={!authenticated || loadingBoards}
                  className="w-full"
                >
                  {loadingBoards ? "로딩 중..." : "보드 목록 조회"}
                </Button>
                
                <Button 
                  onClick={handleCreateBoard} 
                  disabled={!authenticated}
                  variant="outline"
                  className="w-full"
                >
                  새 보드 생성
                </Button>

                {boards.length > 0 && (
                  <div className="mt-4">
                    <p className="text-sm font-medium mb-2">보드 목록 ({boards.length}개):</p>
                    <div className="space-y-1">
                      {boards.map((board) => (
                        <div key={board.boardId || `${board.title}-${board.createdAt}`} className="text-sm p-2 bg-gray-50 rounded">
                          <p className="font-medium">{board.title}</p>
                          <p className="text-gray-600">{board.description}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </ProtectedRoute>
  );
} 