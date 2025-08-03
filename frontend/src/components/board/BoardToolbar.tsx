import { Eye, Plus, Search } from "lucide-react";
import type React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useBoardStore } from "@/store/boardStore";

interface BoardToolbarProps {
	onAddList: () => void;
	onViewOptions: () => void;
	onSearchCards: (searchTerm: string) => void;
}

export const BoardToolbar: React.FC<BoardToolbarProps> = ({
	onAddList,
	onViewOptions,
	onSearchCards,
}) => {
	const { t } = useTranslation("board");
	const { searchTerm, setSearchTerm } = useBoardStore();

	const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const value = e.target.value;
		setSearchTerm(value);
		onSearchCards(value);
	};

	return (
		<div className="flex items-center justify-between px-8 py-4 bg-gray-50 border-b border-gray-200">
			{/* Left side - Add List button */}
			<div className="flex items-center gap-3">
				<Button
					onClick={onAddList}
					className="bg-blue-600 hover:bg-blue-700 text-white"
				>
					<Plus className="h-4 w-4 mr-2" />
					{t("boardDetail.addList")}
				</Button>

				<Button
					variant="outline"
					onClick={onViewOptions}
					className="border-gray-300 text-gray-700 hover:bg-gray-50"
				>
					<Eye className="h-4 w-4 mr-2" />
					{t("boardDetail.viewOptions")}
				</Button>
			</div>

			{/* Right side - Search */}
			<div className="relative">
				<Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
				<Input
					type="text"
					placeholder={t("boardDetail.searchCards")}
					value={searchTerm}
					onChange={handleSearchChange}
					className="pl-10 pr-4 w-64 border-gray-300 focus:border-blue-500 focus:ring-blue-500"
				/>
			</div>
		</div>
	);
};
