// 카드 우선순위에 따른 색상 반환 함수
export const getCardPriorityColor = (priority?: string): string => {
	switch (priority?.toLowerCase()) {
		case "high":
			return "bg-red-100 text-red-800";
		case "medium":
			return "bg-yellow-100 text-yellow-800";
		case "low":
			return "bg-green-100 text-green-800";
		default:
			return "bg-gray-100 text-gray-800";
	}
};
