import type React from "react";
import { cn, getAvatarBackgroundColor, getUserInitials } from "@/lib/utils";
import { useCurrentLanguage } from "@/store/languageStore";

export interface AvatarProps {
	/** 사용자 성 (필수) */
	firstName: string;
	/** 사용자 이름 (필수) */
	lastName: string;
	/** 아바타 크기 (기본값: 'md') */
	size?: "sm" | "md" | "lg" | "xl";
	/** 사용자 정의 클래스명 */
	className?: string;
	/** 사용자 정의 스타일 */
	style?: React.CSSProperties;
	/** 배경색을 위한 사용자 ID (기본값: firstName + lastName) */
	userId?: string;
	/** 그라디언트 배경 사용 여부 (기본값: false) */
	useGradient?: boolean;
	/** 그라디언트 색상 (useGradient이 true일 때만 사용) */
	gradientColors?: {
		from: string;
		to: string;
	};
}

const sizeClasses = {
	sm: "w-6 h-6 text-xs",
	md: "w-8 h-8 text-sm",
	lg: "w-12 h-12 text-base",
	xl: "w-16 h-16 text-lg",
};

export function Avatar({
	firstName,
	lastName,
	size = "md",
	className,
	style,
	userId,
	useGradient = false,
	gradientColors = { from: "from-blue-600", to: "to-purple-600" },
}: AvatarProps) {
	const currentLanguage = useCurrentLanguage();

	// 이니셜 생성 (언어에 따라 순서 다름)
	const initials = getUserInitials(firstName, lastName, currentLanguage);

	// 배경색 결정
	const avatarUserId = userId || `${firstName}${lastName}`;
	const backgroundColor = useGradient
		? `bg-gradient-to-r ${gradientColors.from} ${gradientColors.to}`
		: getAvatarBackgroundColor(avatarUserId);

	return (
		<div
			className={cn(
				"rounded-full flex items-center justify-center text-white font-medium whitespace-nowrap",
				sizeClasses[size],
				backgroundColor,
				className,
			)}
			style={style}
		>
			{initials}
		</div>
	);
}
