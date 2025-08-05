import {
	Calendar,
	MessageSquare,
	MoreHorizontal,
	Paperclip,
} from "lucide-react";
import type React from "react";
import { Button } from "@/components/ui/button";
import type { BoardCardResponse } from "@/services/api/client";
import { useCurrentLanguage } from "@/store/languageStore";
import { getCardPriorityColor } from "@/utils/cardUtils";
import { formatDate } from "@/utils/formatDate";

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
	const currentLanguage = useCurrentLanguage();

	const getInitials = (firstName?: string, lastName?: string) => {
		if (!firstName && !lastName) return "?";
		return `${firstName?.charAt(0) || ""}${lastName?.charAt(0) || ""}`.toUpperCase();
	};

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

			{/* Bottom section */}
			<div className="flex items-center justify-between">
				<div className="flex items-center gap-2">
					{/* Due date */}
					{card.dueDate && (
						<div className="flex items-center gap-1 text-xs text-gray-500">
							<Calendar className="h-3 w-3" />
							<span>{formatDate(card.dueDate, currentLanguage)}</span>
						</div>
					)}

					{/* Comments */}
					{card.commentCount && card.commentCount > 0 && (
						<div className="flex items-center gap-1 text-xs text-gray-500">
							<MessageSquare className="h-3 w-3" />
							<span>{card.commentCount}</span>
						</div>
					)}

					{/* Attachments */}
					{card.attachmentCount && card.attachmentCount > 0 && (
						<div className="flex items-center gap-1 text-xs text-gray-500">
							<Paperclip className="h-3 w-3" />
							<span>{card.attachmentCount}</span>
						</div>
					)}
				</div>

				{/* Assignees */}
				<div className="flex items-center gap-1">
					{card.assignees?.slice(0, 2).map((assignee, index) => (
						<div
							key={assignee.userId}
							className="w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-medium border-2 border-white"
							style={{
								background: "linear-gradient(135deg, #2b7fff, #9810fa)",
								zIndex: 2 - index,
								marginLeft: index > 0 ? "-4px" : "0",
							}}
						>
							{getInitials(assignee.firstName, assignee.lastName)}
						</div>
					))}
					{card.assignees && card.assignees.length > 2 && (
						<div className="w-6 h-6 rounded-full bg-gray-200 flex items-center justify-center text-gray-600 text-xs font-medium border-2 border-white -ml-1">
							+{card.assignees.length - 2}
						</div>
					)}
				</div>
			</div>

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
