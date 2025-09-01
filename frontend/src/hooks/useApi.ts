import { useMemo } from "react";
import {
	createAuthenticatedApiClient,
	publicApi,
} from "@/services/api/authApi";
import { useOAuth } from "./useAuth";

/**
 * 인증 상태에 따른 API 클라이언트를 제공하는 Hook
 * @returns 인증된 API 클라이언트와 공개 API 클라이언트
 */
export function useApi() {
	const { user, isAuthenticated } = useOAuth();

	// access token 추출
	const accessToken = useMemo(() => {
		if (!isAuthenticated || !user?.access_token) {
			return null;
		}
		return user.access_token;
	}, [isAuthenticated, user?.access_token]);

	// 인증된 API 클라이언트 생성
	const authenticatedApi = useMemo(() => {
		if (!accessToken) {
			return null;
		}
		return createAuthenticatedApiClient(accessToken);
	}, [accessToken]);

	return {
		// 인증이 필요한 API들 (access token이 있을 때만 사용 가능)
		authenticated: authenticatedApi,
		// 인증이 필요 없는 API들 (항상 사용 가능)
		public: publicApi,
		// 인증 상태
		isAuthenticated,
		// access token (직접 사용이 필요한 경우)
		accessToken,
	};
}
