import { MoreHorizontal, Plus } from "lucide-react";
import type React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import type {
	BoardCardResponse,
	BoardColumnResponse,
} from "@/services/api/client";
import { BoardCard } from "./BoardCard";

interface BoardColumnProps {
	column: BoardColumnResponse;
	onAddCard: (columnId: string) => void;
	onCardClick: (card: BoardCardResponse) => void;
	onCardMore: (card: BoardCardResponse, e: React.MouseEvent) => void;
	onColumnMore: (e: React.MouseEvent) => void;
}

export const BoardColumn: React.FC<BoardColumnProps> = ({
	column,
	onAddCard,
	onCardClick,
	onCardMore,
	onColumnMore,
}) => {
	const { t } = useTranslation("board");

	const getColumnColor = (color?: string) => {
		if (!color) return "bg-gray-100";

		// 색상별 배경색 매핑
		const colorMap: Record<string, string> = {
			blue: "bg-blue-100",
			green: "bg-green-100",
			yellow: "bg-yellow-100",
			red: "bg-red-100",
			purple: "bg-purple-100",
			pink: "bg-pink-100",
			gray: "bg-gray-100",
		};

		return colorMap[color.toLowerCase()] || "bg-gray-100";
	};

	return (
		<div className="flex-shrink-0 w-80">
			<div className="bg-gray-50 rounded-lg p-4 h-full">
				{/* Column header */}
				<div className="flex items-center justify-between mb-4">
					<div className="flex items-center gap-2">
						<div
							className={`w-4 h-4 rounded ${getColumnColor(column.columnColor)}`}
						/>
						<h3 className="font-semibold text-gray-900">{column.columnName}</h3>
						<span className="bg-white text-gray-600 text-sm font-medium px-2 py-1 rounded-full">
							{column.cardCount || 0}
						</span>
					</div>

					<Button
						variant="ghost"
						size="sm"
						onClick={onColumnMore}
						className="p-1 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
					>
						<MoreHorizontal className="h-4 w-4" />
					</Button>
				</div>

				{/* Cards */}
				<div className="space-y-3 mb-4">
					{column.cards && column.cards.length > 0 ? (
						column.cards.map((card) => (
							<BoardCard
								key={card.cardId}
								card={card}
								onClick={() => onCardClick(card)}
								onMore={(e) => onCardMore(card, e)}
							/>
						))
					) : (
						<div className="text-center py-8 text-gray-500">
							<p className="text-sm">{t("boardDetail.noCards")}</p>
						</div>
					)}
				</div>

				{/* Add card button */}
				<Button
					variant="outline"
					onClick={() => onAddCard(column.columnId || "")}
					className="w-full border-dashed border-gray-300 text-gray-600 hover:bg-gray-50 hover:border-gray-400"
				>
					<Plus className="h-4 w-4 mr-2" />
					{t("boardDetail.addCard")}
				</Button>
			</div>
		</div>
	);
};
