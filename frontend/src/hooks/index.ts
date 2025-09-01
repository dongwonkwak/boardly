export { useApi } from "./useApi";
export { useOAuth } from "./useAuth";
export { useBoard } from "./useBoard";
export { useDashboard } from "./useDashboard";

import { getUserInitials } from "@/lib/utils";
import { useCurrentLanguage } from "@/store/languageStore";

/**
 * 현재 언어 설정을 기반으로 사용자 이니셜을 생성하는 훅
 * @returns 이니셜 생성 함수
 */
export const useUserInitials = () => {
	const currentLanguage = useCurrentLanguage();

	return (firstName?: string, lastName?: string) => {
		return getUserInitials(firstName, lastName, currentLanguage);
	};
};
