// Intl.RelativeTimeFormat을 사용한 국제화된 상대적 시간 포맷팅 함수
export const formatTimeAgo = (timestamp: string, language: string): string => {
	const now = new Date();
	const date = new Date(timestamp);
	const diffInMs = now.getTime() - date.getTime();
	const locale = getLocale(language);

	// 1분 미만
	if (diffInMs < 60 * 1000) {
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			0,
			"second",
		);
	}

	// 1시간 미만 (분 단위)
	if (diffInMs < 60 * 60 * 1000) {
		const diffInMinutes = Math.floor(diffInMs / (60 * 1000));
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			-diffInMinutes,
			"minute",
		);
	}

	// 24시간 미만 (시간 단위)
	if (diffInMs < 24 * 60 * 60 * 1000) {
		const diffInHours = Math.floor(diffInMs / (60 * 60 * 1000));
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			-diffInHours,
			"hour",
		);
	}

	// 7일 미만 (일 단위)
	if (diffInMs < 7 * 24 * 60 * 60 * 1000) {
		const diffInDays = Math.floor(diffInMs / (24 * 60 * 60 * 1000));
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			-diffInDays,
			"day",
		);
	}

	// 4주 미만 (주 단위)
	if (diffInMs < 4 * 7 * 24 * 60 * 60 * 1000) {
		const diffInWeeks = Math.floor(diffInMs / (7 * 24 * 60 * 60 * 1000));
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			-diffInWeeks,
			"week",
		);
	}

	// 12개월 미만 (개월 단위)
	if (diffInMs < 12 * 30 * 24 * 60 * 60 * 1000) {
		const diffInMonths = Math.floor(diffInMs / (30 * 24 * 60 * 60 * 1000));
		return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
			-diffInMonths,
			"month",
		);
	}

	// 1년 이상 (년 단위)
	const diffInYears = Math.floor(diffInMs / (365 * 24 * 60 * 60 * 1000));
	return new Intl.RelativeTimeFormat(locale, { numeric: "auto" }).format(
		-diffInYears,
		"year",
	);
};

// 언어 코드를 로케일로 변환하는 함수
const getLocale = (language: string): string => {
	switch (language) {
		case "ko":
			return "ko-KR";
		case "en":
			return "en-US";
		default:
			return "ko-KR";
	}
};

// 다국어 지원 날짜 포맷팅 함수
export const formatDate = (
	dateString?: string,
	language: string = "ko",
): string => {
	if (!dateString) return "";
	const date = new Date(dateString);
	const locale = getLocale(language);
	return date.toLocaleDateString(locale, { month: "long", day: "numeric" });
};
