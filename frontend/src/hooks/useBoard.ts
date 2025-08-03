import { useCallback } from "react";
import type * as apiClient from "@/services/api/client";
import logger from "@/utils/logger";
import { useBoardStore } from "../store/boardStore";
import { useApi } from "./useApi";

/**
 * 보드 상세 데이터 로딩을 위한 커스텀 훅
 * API 호출과 store 업데이트를 처리합니다.
 */
export function useBoard() {
	const { authenticated } = useApi();
	const {
		setBoard,
		setLoading,
		setError,
		setSearchTerm,
		addCard,
		updateCard,
		deleteCard,
		filterCards,
		clearSearch,
		reset,
	} = useBoardStore();

	// 보드 상세 정보 로딩
	const loadBoardDetail = useCallback(
		async (boardId: string) => {
			if (!authenticated || !boardId) {
				logger.warn("인증되지 않았거나 boardId가 없습니다.", { boardId });
				return;
			}

			try {
				setLoading(true);
				setError(null);

				const result = await (
					authenticated.getBoardDetail as typeof apiClient.getBoardDetail
				)(boardId);

				if (result.status === 200) {
					setBoard(result.data);
					logger.info("보드 상세 정보 로딩 성공", { boardId });
				} else {
					setError("보드 상세 정보를 불러오는데 실패했습니다.");
					logger.error("보드 상세 정보 로딩 실패", {
						boardId,
						status: result.status,
					});
				}
			} catch (error) {
				setError("보드 상세 정보를 불러오는 중 오류가 발생했습니다.");
				logger.error("보드 상세 정보 로딩 중 오류", { boardId, error });
			} finally {
				setLoading(false);
			}
		},
		[authenticated, setBoard, setLoading, setError],
	);

	// 보드 업데이트
	const updateBoardDetail = useCallback(
		async (boardId: string, updates: apiClient.UpdateBoardRequest) => {
			if (!authenticated || !boardId) {
				logger.warn("인증되지 않았거나 boardId가 없습니다.", { boardId });
				return;
			}

			try {
				const result = await (
					authenticated.updateBoard as typeof apiClient.updateBoard
				)(boardId, updates);

				if (result.status === 200) {
					// 현재 보드 정보를 업데이트
					const currentBoard = useBoardStore.getState().board;
					if (currentBoard) {
						setBoard({
							...currentBoard,
							...updates,
						});
					}
					logger.info("보드 업데이트 성공", { boardId, updates });
				} else {
					logger.error("보드 업데이트 실패", {
						boardId,
						status: result.status,
						updates,
					});
				}
			} catch (error) {
				logger.error("보드 업데이트 중 오류", { boardId, error, updates });
			}
		},
		[authenticated, setBoard],
	);

	// 보드 삭제
	const deleteBoardDetail = useCallback(
		async (boardId: string) => {
			if (!authenticated || !boardId) {
				logger.warn("인증되지 않았거나 boardId가 없습니다.", { boardId });
				return;
			}

			try {
				const result = await (
					authenticated.deleteBoard as typeof apiClient.deleteBoard
				)(boardId);

				if (result.status === 204) {
					reset(); // store 초기화
					logger.info("보드 삭제 성공", { boardId });
					return true;
				} else {
					logger.error("보드 삭제 실패", {
						boardId,
						status: result.status,
					});
					return false;
				}
			} catch (error) {
				logger.error("보드 삭제 중 오류", { boardId, error });
				return false;
			}
		},
		[authenticated, reset],
	);

	// 보드 즐겨찾기 토글
	const toggleBoardStar = useCallback(
		async (boardId: string) => {
			if (!authenticated || !boardId) {
				logger.warn("인증되지 않았거나 boardId가 없습니다.", { boardId });
				return;
			}

			try {
				const currentBoard = useBoardStore.getState().board;
				if (!currentBoard) return;

				let result: any;
				if (currentBoard.isStarred) {
					result = await (
						authenticated.unstarBoard as typeof apiClient.unstarBoard
					)(boardId);
				} else {
					result = await (
						authenticated.starBoard as typeof apiClient.starBoard
					)(boardId);
				}

				if (result.status === 200) {
					// 현재 보드 정보 업데이트
					setBoard({
						...currentBoard,
						isStarred: !currentBoard.isStarred,
					});
					logger.info("보드 즐겨찾기 토글 성공", {
						boardId,
						isStarred: !currentBoard.isStarred,
					});
				} else {
					logger.error("보드 즐겨찾기 토글 실패", {
						boardId,
						status: result.status,
					});
				}
			} catch (error) {
				logger.error("보드 즐겨찾기 토글 중 오류", { boardId, error });
			}
		},
		[authenticated, setBoard],
	);

	// 보드 아카이브/언아카이브
	const toggleBoardArchive = useCallback(
		async (boardId: string, archive: boolean) => {
			if (!authenticated || !boardId) {
				logger.warn("인증되지 않았거나 boardId가 없습니다.", { boardId });
				return;
			}

			try {
				let result: any;
				if (archive) {
					result = await (
						authenticated.archiveBoard as typeof apiClient.archiveBoard
					)(boardId);
				} else {
					result = await (
						authenticated.unarchiveBoard as typeof apiClient.unarchiveBoard
					)(boardId);
				}

				if (result.status === 200) {
					logger.info("보드 아카이브 토글 성공", {
						boardId,
						archive,
					});
					// 보드 목록에서 제거되므로 store 초기화
					reset();
				} else {
					logger.error("보드 아카이브 토글 실패", {
						boardId,
						status: result.status,
						archive,
					});
				}
			} catch (error) {
				logger.error("보드 아카이브 토글 중 오류", { boardId, error, archive });
			}
		},
		[authenticated, reset],
	);

	// 카드 생성
	const createCard = useCallback(
		async (listId: string, cardData: apiClient.CreateCardRequest) => {
			if (!authenticated || !listId) {
				logger.warn("인증되지 않았거나 listId가 없습니다.", { listId });
				return;
			}

			try {
				const result = await (
					authenticated.createCard as typeof apiClient.createCard
				)(cardData);

				if (result.status === 201) {
					addCard(listId, result.data);
					logger.info("카드 생성 성공", { listId, cardId: result.data.cardId });
					return result.data;
				} else {
					logger.error("카드 생성 실패", {
						listId,
						status: result.status,
						cardData,
					});
					return null;
				}
			} catch (error) {
				logger.error("카드 생성 중 오류", { listId, error, cardData });
				return null;
			}
		},
		[authenticated, addCard],
	);

	// 카드 업데이트
	const updateCardDetail = useCallback(
		async (cardId: string, updates: apiClient.UpdateCardRequest) => {
			if (!authenticated || !cardId) {
				logger.warn("인증되지 않았거나 cardId가 없습니다.", { cardId });
				return;
			}

			try {
				const result = await (
					authenticated.updateCard as typeof apiClient.updateCard
				)(cardId, updates);

				if (result.status === 200) {
					updateCard(cardId, updates);
					logger.info("카드 업데이트 성공", { cardId, updates });
					return result.data;
				} else {
					logger.error("카드 업데이트 실패", {
						cardId,
						status: result.status,
						updates,
					});
					return null;
				}
			} catch (error) {
				logger.error("카드 업데이트 중 오류", { cardId, error, updates });
				return null;
			}
		},
		[authenticated, updateCard],
	);

	// 카드 삭제
	const deleteCardDetail = useCallback(
		async (cardId: string) => {
			if (!authenticated || !cardId) {
				logger.warn("인증되지 않았거나 cardId가 없습니다.", { cardId });
				return;
			}

			try {
				const result = await (
					authenticated.deleteCard as typeof apiClient.deleteCard
				)(cardId);

				if (result.status === 204) {
					deleteCard(cardId);
					logger.info("카드 삭제 성공", { cardId });
					return true;
				} else {
					logger.error("카드 삭제 실패", {
						cardId,
						status: result.status,
					});
					return false;
				}
			} catch (error) {
				logger.error("카드 삭제 중 오류", { cardId, error });
				return false;
			}
		},
		[authenticated, deleteCard],
	);

	// 카드 이동
	const moveCardDetail = useCallback(
		async (cardId: string, moveData: apiClient.MoveCardRequest) => {
			if (!authenticated || !cardId) {
				logger.warn("인증되지 않았거나 cardId가 없습니다.", { cardId });
				return;
			}

			try {
				const result = await (
					authenticated.moveCard as typeof apiClient.moveCard
				)(cardId, moveData);

				if (result.status === 200) {
					// 현재 보드에서 카드 위치 업데이트
					const currentBoard = useBoardStore.getState().board;
					if (
						currentBoard &&
						moveData.targetListId &&
						moveData.newPosition !== undefined
					) {
						// 기존 위치에서 카드 제거
						const updatedColumns = currentBoard.columns?.map((column) => ({
							...column,
							cards:
								column.cards?.filter((card) => card.cardId !== cardId) || [],
						}));

						// 새 위치에 카드 추가
						const targetColumn = updatedColumns?.find(
							(col) => col.columnId === moveData.targetListId,
						);
						if (targetColumn) {
							const movedCard = currentBoard.columns
								?.flatMap((col) => col.cards || [])
								.find((card) => card.cardId === cardId);

							if (movedCard) {
								const newCards = [...(targetColumn.cards || [])];
								newCards.splice(moveData.newPosition!, 0, movedCard);

								const finalColumns = updatedColumns?.map((col) =>
									col.columnId === moveData.targetListId
										? { ...col, cards: newCards }
										: col,
								);

								setBoard({
									...currentBoard,
									columns: finalColumns,
								});
							}
						}
					}

					logger.info("카드 이동 성공", { cardId, moveData });
					return true;
				} else {
					logger.error("카드 이동 실패", {
						cardId,
						status: result.status,
						moveData,
					});
					return false;
				}
			} catch (error) {
				logger.error("카드 이동 중 오류", { cardId, error, moveData });
				return false;
			}
		},
		[authenticated, setBoard],
	);

	// 검색 쿼리 설정
	const setSearchQuery = useCallback(
		(query: string) => {
			setSearchTerm(query);
		},
		[setSearchTerm],
	);

	// 에러 초기화
	const resetErrors = useCallback(() => {
		setError(null);
	}, [setError]);

	// 데이터 초기화
	const clearData = useCallback(() => {
		reset();
	}, [reset]);

	return {
		// 데이터 로딩
		loadBoardDetail,

		// 보드 관리
		updateBoardDetail,
		deleteBoardDetail,
		toggleBoardStar,
		toggleBoardArchive,

		// 카드 관리
		createCard,
		updateCardDetail,
		deleteCardDetail,
		moveCardDetail,

		// 검색 및 필터링
		setSearchQuery,
		filterCards,
		clearSearch,

		// 상태 관리
		resetErrors,
		clearData,
	};
}
