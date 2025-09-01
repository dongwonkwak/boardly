import { Calendar, MessageSquare, Paperclip } from "lucide-react";
import { Avatar } from "@/components/ui/avatar";
import type { BoardCardResponse } from "@/services/api/client";
import { useCurrentLanguage } from "@/store/languageStore";
import { formatDate } from "@/utils/formatDate";

interface BoardCardFooterProps {
	card: BoardCardResponse;
}

export const BoardCardFooter: React.FC<BoardCardFooterProps> = ({ card }) => {
	const currentLanguage = useCurrentLanguage();

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
						<Avatar
							key={assignee.userId}
							firstName={assignee.firstName || ""}
							lastName={assignee.lastName || ""}
							size="sm"
							useGradient={true}
							gradientColors={{ from: "from-blue-500", to: "to-purple-600" }}
							className="border-2 border-white shadow-sm"
							style={{ zIndex: 3 - index }}
							userId={assignee.userId}
						/>
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
