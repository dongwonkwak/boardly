import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

/**
 * 사용자 ID를 기반으로 아바타 배경색을 생성합니다.
 * @param userId - 사용자 ID
 * @returns Tailwind CSS 배경색 클래스
 */
export function getAvatarBackgroundColor(userId: string): string {
	const colors = [
		"bg-blue-500",
		"bg-green-500",
		"bg-purple-500",
		"bg-pink-500",
		"bg-indigo-500",
		"bg-yellow-500",
		"bg-red-500",
		"bg-teal-500",
		"bg-orange-500",
		"bg-cyan-500",
	];

	// userId의 해시값을 계산하여 일관된 색상 선택
	let hash = 0;
	for (let i = 0; i < userId.length; i++) {
		const char = userId.charCodeAt(i);
		hash = (hash << 5) - hash + char;
		hash = hash & hash; // 32비트 정수로 변환
	}

	return colors[Math.abs(hash) % colors.length];
}

/**
 * 사용자 이름의 첫 글자를 추출합니다.
 * @param firstName - 사용자 이름
 * @returns 대문자로 변환된 첫 글자
 */
export function getInitials(firstName?: string): string {
	const first = firstName?.charAt(0) || "";
	return first.toUpperCase();
}

/**
 * 사용자의 이름과 성에서 이니셜을 추출합니다.
 * locale에 따라 순서가 다릅니다:
 * - 한국어(ko): 성 + 이름 순서
 * - 영어(en): 이름 + 성 순서
 * @param firstName - 사용자 이름
 * @param lastName - 사용자 성
 * @param locale - 언어 설정 (기본값: 'ko')
 * @returns 대문자로 변환된 이니셜
 */
export function getUserInitials(
	firstName?: string,
	lastName?: string,
	locale: string = "ko",
): string {
	// 빈 문자열도 유효한 값으로 처리
	if (firstName === undefined && lastName === undefined) return "?";

	const first = firstName?.charAt(0) || "";
	const last = lastName?.charAt(0) || "";

	// 한국어는 성+이름 순서, 영어는 이름+성 순서
	if (locale === "ko") {
		return `${last}${first}`.toUpperCase();
	} else {
		return `${first}${last}`.toUpperCase();
	}
}

/**
 * 보드 그라디언트 테마 색상 정의
 */
export const boardGradientThemes = {
	"blue-purple": "bg-gradient-to-br from-blue-500 to-purple-600",
	"green-teal": "bg-gradient-to-br from-green-500 to-teal-600",
	"orange-red": "bg-gradient-to-br from-orange-500 to-red-600",
	"purple-pink": "bg-gradient-to-br from-purple-500 to-pink-600",
	"indigo-blue": "bg-gradient-to-br from-indigo-500 to-blue-600",
	"pink-rose": "bg-gradient-to-br from-pink-500 to-rose-600",
} as const;

export type BoardThemeKey = keyof typeof boardGradientThemes;

/**
 * 보드 ID와 색상 설정을 기반으로 그라디언트 테마 색상을 반환합니다.
 * @param boardId - 보드 ID
 * @param boardColor - 보드에서 설정한 색상 (선택사항)
 * @returns Tailwind CSS 그라디언트 클래스
 */
export function getBoardThemeColor(
	boardId: string,
	boardColor?: string,
): string {
	const themeKeys = Object.keys(boardGradientThemes) as BoardThemeKey[];

	if (boardColor && boardColor in boardGradientThemes) {
		return boardGradientThemes[boardColor as BoardThemeKey];
	}

	// boardId를 기반으로 일관된 색상 선택
	const index = (Number(boardId) || 0) % themeKeys.length;
	return boardGradientThemes[themeKeys[index]];
}
