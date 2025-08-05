import { ArrowLeft, MoreHorizontal, Plus, Share2, Star } from "lucide-react";
import type React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { useUserInitials } from "@/hooks";
import type { BoardDetailResponse } from "@/services/api/client";

interface BoardHeaderProps {
	board: BoardDetailResponse;
	onBack: () => void;
	onStarToggle: () => void;
	onShare: () => void;
	onMore: () => void;
	onInvite: () => void;
}

export const BoardHeader: React.FC<BoardHeaderProps> = ({
	board,
	onBack,
	onStarToggle,
	onShare,
	onMore,
	onInvite,
}) => {
	const { t } = useTranslation("board");
	const getUserInitialsWithLocale = useUserInitials();

	return (
		<header className="sticky top-0 z-10 bg-white/90 backdrop-blur-sm border-b border-gray-200">
			<div className="flex items-center justify-between px-8 py-4">
				{/* Left side */}
				<div className="flex items-center gap-4">
					<Button variant="ghost" size="sm" onClick={onBack} className="p-2">
						<ArrowLeft className="h-5 w-5" />
					</Button>

					{/* Board icon and title */}
					<div className="flex items-center gap-3">
						<div
							className="w-8 h-8 rounded-lg flex items-center justify-center text-white font-bold text-sm"
							style={{
								background: board.boardColor
									? `linear-gradient(135deg, ${board.boardColor}20, ${board.boardColor}40)`
									: "linear-gradient(135deg, #2b7fff, #9810fa)",
							}}
						>
							{board.boardName?.charAt(0) || "B"}
						</div>
						<div>
							<h1 className="text-lg font-semibold text-gray-900">
								{board.boardName}
							</h1>
							<p className="text-sm text-gray-600">{board.boardDescription}</p>
						</div>
					</div>
				</div>

				{/* Right side */}
				<div className="flex items-center gap-2">
					{/* Member avatars */}
					<div className="flex items-center gap-1">
						{board.boardMembers?.slice(0, 3).map((member, index) => (
							<div
								key={member.userId}
								className="w-8 h-8 rounded-full flex items-center justify-center text-white text-sm font-medium border-2 border-white"
								style={{
									background: "linear-gradient(135deg, #2b7fff, #9810fa)",
									zIndex: 3 - index,
									marginLeft: index > 0 ? "-8px" : "0",
								}}
							>
								{getUserInitialsWithLocale(member.firstName, member.lastName)}
							</div>
						))}
						{board.boardMembers && board.boardMembers.length > 3 && (
							<div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-gray-600 text-xs font-medium border-2 border-white -ml-2">
								+{board.boardMembers.length - 3}
							</div>
						)}
					</div>

					{/* Action buttons */}
					<Button
						variant="outline"
						size="sm"
						onClick={onInvite}
						className="ml-2"
					>
						<Plus className="h-4 w-4 mr-1" />
						{t("boardDetail.invite")}
					</Button>

					<Button
						variant="ghost"
						size="sm"
						onClick={onStarToggle}
						className="p-2"
					>
						<Star
							className={`h-5 w-5 ${board.isStarred ? "fill-yellow-400 text-yellow-400" : "text-gray-400"}`}
						/>
					</Button>

					<Button variant="ghost" size="sm" onClick={onShare} className="p-2">
						<Share2 className="h-5 w-5 text-gray-400" />
					</Button>

					<Button variant="ghost" size="sm" onClick={onMore} className="p-2">
						<MoreHorizontal className="h-5 w-5 text-gray-400" />
					</Button>
				</div>
			</div>
		</header>
	);
};
