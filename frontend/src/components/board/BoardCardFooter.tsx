import { Calendar, MessageSquare, Paperclip } from "lucide-react";
import { useUserInitials } from "@/hooks";
import { getUserInitials } from "@/lib/utils";
import type { BoardCardResponse } from "@/services/api/client";
import { useCurrentLanguage } from "@/store/languageStore";
import { formatDate } from "@/utils/formatDate";

interface BoardCardFooterProps {
	card: BoardCardResponse;
}

export const BoardCardFooter: React.FC<BoardCardFooterProps> = ({ card }) => {
	const currentLanguage = useCurrentLanguage();
	const getUserInitialsWithLocale = useUserInitials();

	return (
		<div className="flex items-center justify-between mt-3">
			{/* Left side - Metadata */}
			<div className="flex items-center gap-3">
				{/* Due date */}
				{card.dueDate && (
					<div className="flex items-center gap-1 text-xs text-gray-500">
						<Calendar className="h-3 w-3" />
						<span>{formatDate(card.dueDate, currentLanguage)}</span>
					</div>
				)}

				{/* Comments */}
				{card.commentCount !== undefined && (
					<div className="flex items-center text-xs text-gray-500">
						<MessageSquare className="w-3 h-3 mr-1" />
						{card.commentCount}
					</div>
				)}

				{/* Attachments */}
				{card.attachmentCount !== undefined && (
					<div className="flex items-center text-xs text-gray-500">
						<Paperclip className="w-3 h-3 mr-1" />
						{card.attachmentCount}
					</div>
				)}
			</div>

			{/* Right side - Assignees */}
			{card.assignees && card.assignees.length > 0 && (
				<div className="flex items-center -space-x-1">
					{card.assignees.slice(0, 3).map((assignee, index) => (
						<div
							key={assignee.userId}
							className="w-6 h-6 rounded-full flex items-center justify-center text-white text-xs font-medium border-2 border-white shadow-sm whitespace-nowrap"
							style={{
								background: "linear-gradient(135deg, #2b7fff, #9810fa)",
								zIndex: 3 - index,
							}}
							title={`${assignee.firstName} ${assignee.lastName}`}
						>
							{getUserInitialsWithLocale(assignee.firstName, assignee.lastName)}
						</div>
					))}
					{card.assignees.length > 3 && (
						<div className="w-6 h-6 rounded-full bg-gray-200 flex items-center justify-center text-gray-600 text-xs font-medium border-2 border-white shadow-sm">
							+{card.assignees.length - 3}
						</div>
					)}
				</div>
			)}
		</div>
	);
};
