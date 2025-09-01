import { MoreHorizontal } from "lucide-react";
import type React from "react";
import { Button } from "@/components/ui/button";
import type { BoardCardResponse } from "@/services/api/client";
import { getCardPriorityColor } from "@/utils/cardUtils";
import { BoardCardFooter } from "./BoardCardFooter";

interface BoardCardProps {
	card: BoardCardResponse;
	onClick: () => void;
	onMore: (e: React.MouseEvent) => void;
}

export const BoardCard: React.FC<BoardCardProps> = ({
	card,
	onClick,
	onMore,
}) => {
	return (
		<div
			role="button"
			tabIndex={0}
			onClick={onClick}
			onKeyDown={(e) => {
				if (e.key === "Enter" || e.key === " ") {
					e.preventDefault();
					onClick();
				}
			}}
			className="bg-white border border-gray-200 rounded-lg p-4 cursor-pointer hover:shadow-md transition-shadow relative group text-left w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
		>
			{/* Labels */}
			{card.labels && card.labels.length > 0 && (
				<div className="flex flex-wrap gap-1 mb-3">
					{card.labels.map((label) => (
						<span
							key={label.id}
							className="px-2 py-1 rounded-full text-xs font-medium"
							style={{
								backgroundColor: `${label.color}20`,
								color: label.color,
							}}
						>
							{label.name}
						</span>
					))}
				</div>
			)}

			{/* Title */}
			<h4 className="font-medium text-gray-900 mb-2 line-clamp-2">
				{card.title}
			</h4>

			{/* Description */}
			{card.description && (
				<p className="text-sm text-gray-600 mb-3 line-clamp-2">
					{card.description}
				</p>
			)}

			{/* Priority */}
			{card.priority && (
				<div className="mb-3">
					<span
						className={`px-2 py-1 rounded-full text-xs font-medium ${getCardPriorityColor(card.priority)}`}
					>
						{card.priority}
					</span>
				</div>
			)}

			{/* Footer */}
			<BoardCardFooter card={card} />

			{/* More button */}
			<Button
				variant="ghost"
				size="sm"
				className="absolute top-2 right-2 p-1 h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
				onClick={(e) => {
					e.stopPropagation();
					onMore(e);
				}}
			>
				<MoreHorizontal className="h-3 w-3" />
			</Button>
		</div>
	);
};
