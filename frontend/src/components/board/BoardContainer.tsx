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
					<div className="bg-gray-50 rounded-lg p-4 h-full">
						{/* Header height matching section */}
						<div className="flex items-center justify-between mb-4">
							<div className="flex items-center gap-2">
								<div className="w-4 h-4 rounded bg-gray-200" />
								<h3 className="font-semibold text-gray-900">
									{t("boardDetail.addAnotherList")}
								</h3>
								<span className="bg-white text-gray-600 text-sm font-medium px-2 py-1 rounded-full">
									0
								</span>
							</div>
						</div>

						{/* Add list button */}
						<Button
							variant="outline"
							onClick={onAddList}
							className="w-full border-dashed border-gray-300 text-gray-600 hover:bg-gray-50 hover:border-gray-400 flex items-center justify-center py-8"
						>
							<Plus className="h-6 w-6 mr-2" />
							<span className="text-sm whitespace-nowrap">{t("boardDetail.addAnotherList")}</span>
						</Button>
					</div>
				</div>
			</div>
		</div>
	);
};
