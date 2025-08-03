import { create } from "zustand";
import { devtools } from "zustand/middleware";
import type {
	BoardCardResponse,
	BoardColumnResponse,
	BoardDetailResponse,
	BoardLabelResponse,
	BoardMemberResponse,
} from "@/services/api/client";
import logger from "@/utils/logger";

export interface BoardState {
	// 보드 기본 정보
	board: BoardDetailResponse | null;
	isLoading: boolean;
	error: string | null;

	// 검색 및 필터링
	searchTerm: string;
	filteredCards: BoardCardResponse[];

	// UI 상태
	selectedCard: BoardCardResponse | null;
	selectedColumn: BoardColumnResponse | null;
	isCardModalOpen: boolean;
	isColumnModalOpen: boolean;
	isAddCardModalOpen: boolean;
	isAddListModalOpen: boolean;

	// 드래그 앤 드롭 상태
	draggedCard: BoardCardResponse | null;
	draggedColumn: BoardColumnResponse | null;
	dropTarget: { columnId: string; position: number } | null;

	// 액션 함수들
	setBoard: (board: BoardDetailResponse | null) => void;
	setLoading: (loading: boolean) => void;
	setError: (error: string | null) => void;
	setSearchTerm: (term: string) => void;
	setSelectedCard: (card: BoardCardResponse | null) => void;
	setSelectedColumn: (column: BoardColumnResponse | null) => void;
	setCardModalOpen: (open: boolean) => void;
	setColumnModalOpen: (open: boolean) => void;
	setAddCardModalOpen: (open: boolean) => void;
	setAddListModalOpen: (open: boolean) => void;
	setDraggedCard: (card: BoardCardResponse | null) => void;
	setDraggedColumn: (column: BoardColumnResponse | null) => void;
	setDropTarget: (
		target: { columnId: string; position: number } | null,
	) => void;

	// 카드 관련 액션
	addCard: (columnId: string, card: BoardCardResponse) => void;
	updateCard: (cardId: string, updates: Partial<BoardCardResponse>) => void;
	deleteCard: (cardId: string) => void;
	moveCard: (
		cardId: string,
		fromColumnId: string,
		toColumnId: string,
		newPosition: number,
	) => void;

	// 컬럼 관련 액션
	addColumn: (column: BoardColumnResponse) => void;
	updateColumn: (
		columnId: string,
		updates: Partial<BoardColumnResponse>,
	) => void;
	deleteColumn: (columnId: string) => void;
	moveColumn: (columnId: string, newPosition: number) => void;

	// 멤버 관련 액션
	addMember: (member: BoardMemberResponse) => void;
	removeMember: (userId: string) => void;
	updateMemberRole: (userId: string, role: string) => void;

	// 라벨 관련 액션
	addLabel: (label: BoardLabelResponse) => void;
	updateLabel: (labelId: string, updates: Partial<BoardLabelResponse>) => void;
	deleteLabel: (labelId: string) => void;

	// 검색 및 필터링 액션
	filterCards: () => void;
	clearSearch: () => void;

	// 초기화
	reset: () => void;
}

const initialState = {
	board: null,
	isLoading: false,
	error: null,
	searchTerm: "",
	filteredCards: [],
	selectedCard: null,
	selectedColumn: null,
	isCardModalOpen: false,
	isColumnModalOpen: false,
	isAddCardModalOpen: false,
	isAddListModalOpen: false,
	draggedCard: null,
	draggedColumn: null,
	dropTarget: null,
};

export const useBoardStore = create<BoardState>()(
	devtools(
		(set, get) => ({
			...initialState,

			// 기본 상태 설정
			setBoard: (board) => {
				set({ board, error: null });
				logger.info("Board set in store", { boardId: board?.boardId });
			},

			setLoading: (loading) => {
				set({ isLoading: loading });
			},

			setError: (error) => {
				set({ error, isLoading: false });
				if (error) {
					logger.error("Board store error", { error });
				}
			},

			setSearchTerm: (term) => {
				set({ searchTerm: term });
				get().filterCards();
			},

			// UI 상태 설정
			setSelectedCard: (card) => {
				set({ selectedCard: card });
			},

			setSelectedColumn: (column) => {
				set({ selectedColumn: column });
			},

			setCardModalOpen: (open) => {
				set({ isCardModalOpen: open });
			},

			setColumnModalOpen: (open) => {
				set({ isColumnModalOpen: open });
			},

			setAddCardModalOpen: (open) => {
				set({ isAddCardModalOpen: open });
			},

			setAddListModalOpen: (open) => {
				set({ isAddListModalOpen: open });
			},

			// 드래그 앤 드롭 상태 설정
			setDraggedCard: (card) => {
				set({ draggedCard: card });
			},

			setDraggedColumn: (column) => {
				set({ draggedColumn: column });
			},

			setDropTarget: (target) => {
				set({ dropTarget: target });
			},

			// 카드 관련 액션
			addCard: (columnId, card) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: board.columns?.map((column) => {
						if (column.columnId === columnId) {
							return {
								...column,
								cards: [...(column.cards || []), card],
								cardCount: (column.cardCount || 0) + 1,
							};
						}
						return column;
					}),
				};

				set({ board: updatedBoard });
				get().filterCards();
				logger.info("Card added", { columnId, cardId: card.cardId });
			},

			updateCard: (cardId, updates) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: board.columns?.map((column) => ({
						...column,
						cards: column.cards?.map((card) =>
							card.cardId === cardId ? { ...card, ...updates } : card,
						),
					})),
				};

				set({ board: updatedBoard });
				get().filterCards();
				logger.info("Card updated", { cardId, updates });
			},

			deleteCard: (cardId) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: board.columns?.map((column) => ({
						...column,
						cards: column.cards?.filter((card) => card.cardId !== cardId),
						cardCount:
							(column.cardCount || 0) -
							(column.cards?.filter((card) => card.cardId === cardId).length ||
								0),
					})),
				};

				set({ board: updatedBoard });
				get().filterCards();
				logger.info("Card deleted", { cardId });
			},

			moveCard: (cardId, fromColumnId, toColumnId, newPosition) => {
				const { board } = get();
				if (!board) return;

				// 카드 찾기
				let cardToMove: BoardCardResponse | undefined;
				const updatedBoard = {
					...board,
					columns: board.columns?.map((column) => {
						if (column.columnId === fromColumnId) {
							const filteredCards =
								column.cards?.filter((card) => card.cardId !== cardId) || [];
							cardToMove = column.cards?.find((card) => card.cardId === cardId);
							return {
								...column,
								cards: filteredCards,
								cardCount: filteredCards.length,
							};
						}
						return column;
					}),
				};

				// 새 위치에 카드 추가
				if (cardToMove) {
					updatedBoard.columns = updatedBoard.columns?.map((column) => {
						if (column.columnId === toColumnId) {
							const cards = [...(column.cards || [])];
							cards.splice(newPosition, 0, {
								...cardToMove!,
								position: newPosition,
							});
							return {
								...column,
								cards: cards.map((card, index) => ({
									...card,
									position: index,
								})),
								cardCount: cards.length,
							};
						}
						return column;
					});
				}

				set({ board: updatedBoard });
				get().filterCards();
				logger.info("Card moved", {
					cardId,
					fromColumnId,
					toColumnId,
					newPosition,
				});
			},

			// 컬럼 관련 액션
			addColumn: (column) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: [...(board.columns || []), column],
				};

				set({ board: updatedBoard });
				logger.info("Column added", { columnId: column.columnId });
			},

			updateColumn: (columnId, updates) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: board.columns?.map((column) =>
						column.columnId === columnId ? { ...column, ...updates } : column,
					),
				};

				set({ board: updatedBoard });
				logger.info("Column updated", { columnId, updates });
			},

			deleteColumn: (columnId) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					columns: board.columns?.filter(
						(column) => column.columnId !== columnId,
					),
				};

				set({ board: updatedBoard });
				logger.info("Column deleted", { columnId });
			},

			moveColumn: (columnId, newPosition) => {
				const { board } = get();
				if (!board || !board.columns) return;

				const columns = [...board.columns];
				const columnIndex = columns.findIndex(
					(col) => col.columnId === columnId,
				);
				if (columnIndex === -1) return;

				const [movedColumn] = columns.splice(columnIndex, 1);
				columns.splice(newPosition, 0, movedColumn);

				const updatedBoard = {
					...board,
					columns: columns.map((column, index) => ({
						...column,
						position: index,
					})),
				};

				set({ board: updatedBoard });
				logger.info("Column moved", { columnId, newPosition });
			},

			// 멤버 관련 액션
			addMember: (member) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					boardMembers: [...(board.boardMembers || []), member],
				};

				set({ board: updatedBoard });
				logger.info("Member added", { userId: member.userId });
			},

			removeMember: (userId) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					boardMembers: board.boardMembers?.filter(
						(member) => member.userId !== userId,
					),
				};

				set({ board: updatedBoard });
				logger.info("Member removed", { userId });
			},

			updateMemberRole: (userId, role) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					boardMembers: board.boardMembers?.map((member) =>
						member.userId === userId ? { ...member, role } : member,
					),
				};

				set({ board: updatedBoard });
				logger.info("Member role updated", { userId, role });
			},

			// 라벨 관련 액션
			addLabel: (label) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					labels: [...(board.labels || []), label],
				};

				set({ board: updatedBoard });
				logger.info("Label added", { labelId: label.id });
			},

			updateLabel: (labelId, updates) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					labels: board.labels?.map((label) =>
						label.id === labelId ? { ...label, ...updates } : label,
					),
				};

				set({ board: updatedBoard });
				logger.info("Label updated", { labelId, updates });
			},

			deleteLabel: (labelId) => {
				const { board } = get();
				if (!board) return;

				const updatedBoard = {
					...board,
					labels: board.labels?.filter((label) => label.id !== labelId),
				};

				set({ board: updatedBoard });
				logger.info("Label deleted", { labelId });
			},

			// 검색 및 필터링 액션
			filterCards: () => {
				const { board, searchTerm } = get();
				if (!board || !searchTerm.trim()) {
					set({ filteredCards: [] });
					return;
				}

				const allCards: BoardCardResponse[] = [];
				board.columns?.forEach((column) => {
					column.cards?.forEach((card) => {
						allCards.push(card);
					});
				});

				const filtered = allCards.filter(
					(card) =>
						card.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
						card.description?.toLowerCase().includes(searchTerm.toLowerCase()),
				);

				set({ filteredCards: filtered });
				logger.info("Cards filtered", {
					searchTerm,
					resultCount: filtered.length,
				});
			},

			clearSearch: () => {
				set({ searchTerm: "", filteredCards: [] });
				logger.info("Search cleared");
			},

			// 초기화
			reset: () => {
				set(initialState);
				logger.info("Board store reset");
			},
		}),
		{
			name: "board-store",
		},
	),
);
