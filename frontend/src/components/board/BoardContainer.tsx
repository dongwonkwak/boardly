import { Plus } from "lucide-react";
import type React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import type {
	BoardCardResponse,
	BoardColumnResponse,
} from "@/services/api/client";
import { useBoardStore } from "@/store/boardStore";
import { BoardColumn } from "./BoardColumn";

interface BoardContainerProps {
	columns: BoardColumnResponse[];
	onAddList: () => void;
	onAddCard: (columnId: string) => void;
	onCardClick: (card: BoardCardResponse) => void;
	onCardMore: (card: BoardCardResponse, e: React.MouseEvent) => void;
	onColumnMore: (e: React.MouseEvent) => void;
}

export const BoardContainer: React.FC<BoardContainerProps> = ({
	columns,
	onAddList,
	onAddCard,
	onCardClick,
	onCardMore,
	onColumnMore,
}) => {
	const { t } = useTranslation("board");
	const { filteredCards } = useBoardStore();

	return (
		<div className="flex-1 overflow-x-auto">
			<div className="flex gap-6 p-8 min-h-full">
				{/* Existing columns */}
				{columns.map((column) => (
					<BoardColumn
						key={column.columnId}
						column={column}
						onAddCard={onAddCard}
						onCardClick={onCardClick}
						onCardMore={onCardMore}
						onColumnMore={onColumnMore}
					/>
				))}

				{/* Add new list button */}
				<div className="flex-shrink-0 w-80">
					<Button
						variant="outline"
						onClick={onAddList}
						className="w-full h-60 border-dashed border-gray-300 text-gray-600 hover:bg-gray-50 hover:border-gray-400 flex flex-col items-center justify-center"
					>
						<Plus className="h-6 w-6 mb-2" />
						<span className="text-sm">{t("boardDetail.addAnotherList")}</span>
					</Button>
				</div>
			</div>
		</div>
	);
};
