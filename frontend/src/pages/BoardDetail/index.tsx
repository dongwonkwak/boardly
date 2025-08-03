import type React from "react";
import { useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { BoardContainer } from "@/components/board/BoardContainer";
import { BoardHeader } from "@/components/board/BoardHeader";
import { BoardToolbar } from "@/components/board/BoardToolbar";
import LoadingButton from "@/components/common/LoadingButton";
import { useBoard } from "@/hooks";
import type { BoardCardResponse } from "@/services/api/client";
import { useBoardStore } from "@/store/boardStore";
import logger from "@/utils/logger";

export default function BoardDetailPage() {
	const { boardId } = useParams<{ boardId: string }>();
	const navigate = useNavigate();
	const { t } = useTranslation("board");

	const { board, isLoading, error } = useBoardStore();

	const { loadBoardDetail, toggleBoardStar, setSearchQuery, clearData } =
		useBoard();

	useEffect(() => {
		if (boardId) {
			loadBoardDetail(boardId);
		}
	}, [boardId, loadBoardDetail]);

	// 컴포넌트 언마운트 시 store 초기화
	useEffect(() => {
		return () => {
			clearData();
		};
	}, [clearData]);

	const handleBack = () => {
		navigate("/dashboard");
	};

	const handleStarToggle = async () => {
		if (boardId) {
			await toggleBoardStar(boardId);
		}
		logger.info("Star toggle clicked", { boardId });
	};

	const handleShare = () => {
		// TODO: Implement share functionality
		logger.info("Share clicked", { boardId });
	};

	const handleMore = () => {
		// TODO: Implement more options functionality
		logger.info("More options clicked", { boardId });
	};

	const handleInvite = () => {
		// TODO: Implement invite functionality
		logger.info("Invite clicked", { boardId });
	};

	const handleAddList = () => {
		// TODO: Implement add list functionality
		logger.info("Add list clicked", { boardId });
	};

	const handleViewOptions = () => {
		// TODO: Implement view options functionality
		logger.info("View options clicked", { boardId });
	};

	const handleSearchCards = (term: string) => {
		setSearchQuery(term);
		logger.info("Search cards", { boardId, searchTerm: term });
	};

	const handleAddCard = (columnId: string) => {
		// TODO: Implement add card functionality
		logger.info("Add card clicked", { boardId, columnId });
	};

	const handleCardClick = (card: BoardCardResponse) => {
		// TODO: Navigate to card detail or open card modal
		logger.info("Card clicked", { boardId, cardId: card.cardId });
	};

	const handleCardMore = (card: BoardCardResponse, e: React.MouseEvent) => {
		e.stopPropagation();
		// TODO: Implement card more options functionality
		logger.info("Card more options clicked", { boardId, cardId: card.cardId });
	};

	const handleColumnMore = () => {
		// TODO: Implement column more options functionality
		logger.info("Column more options clicked", { boardId });
	};

	if (isLoading) {
		return (
			<div className="min-h-screen bg-gray-50 flex items-center justify-center">
				<div className="text-center">
					<LoadingButton>Loading...</LoadingButton>
					<p className="mt-4 text-gray-600">{t("boardDetail.loading")}</p>
				</div>
			</div>
		);
	}

	if (error || !board) {
		return (
			<div className="min-h-screen bg-gray-50 flex items-center justify-center">
				<div className="text-center">
					<p className="text-red-600 mb-4">{error || t("boardDetail.error")}</p>
					<button
						type="button"
						onClick={() => boardId && loadBoardDetail(boardId)}
						className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
					>
						{t("boardDetail.retry")}
					</button>
				</div>
			</div>
		);
	}

	return (
		<div className="min-h-screen bg-gray-50 flex flex-col">
			{/* Header */}
			<BoardHeader
				board={board}
				onBack={handleBack}
				onStarToggle={handleStarToggle}
				onShare={handleShare}
				onMore={handleMore}
				onInvite={handleInvite}
			/>

			{/* Toolbar */}
			<BoardToolbar
				onAddList={handleAddList}
				onViewOptions={handleViewOptions}
				onSearchCards={handleSearchCards}
			/>

			{/* Board content */}
			<div className="flex-1 flex flex-col">
				{board.columns && board.columns.length > 0 ? (
					<BoardContainer
						columns={board.columns}
						onAddList={handleAddList}
						onAddCard={handleAddCard}
						onCardClick={handleCardClick}
						onCardMore={handleCardMore}
						onColumnMore={handleColumnMore}
					/>
				) : (
					<div className="flex-1 flex items-center justify-center">
						<div className="text-center">
							<p className="text-gray-500 mb-4">{t("boardDetail.noLists")}</p>
							<button
								type="button"
								onClick={handleAddList}
								className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
							>
								{t("boardDetail.addList")}
							</button>
						</div>
					</div>
				)}
			</div>
		</div>
	);
}
