import { useCallback } from 'react';
import { useApi } from './useApi';
import { useDashboardStore } from '@/store/dashboardStore';
import type * as apiClient from '@/services/api/client';

/**
 * 대시보드 데이터 로딩을 위한 커스텀 훅
 * API 호출과 store 업데이트를 처리합니다.
 */
export function useDashboard() {
  const { authenticated } = useApi();
  const {
    updateBoard,
    removeBoard,
    archiveBoard,
    setSearchQuery,
    setIncludeArchived,
    resetErrors,
    clearData
  } = useDashboardStore();

  // 보드 목록 로딩
  const handleLoadBoards = useCallback(async (includeArchived = false) => {
    if (!authenticated) {
      console.log("인증되지 않은 상태입니다.");
      return;
    }

    try {
      const result = await (authenticated.getMyBoards as typeof apiClient.getMyBoards)({ 
        includeArchived 
      });
      
      if (result.status === 200) {
        // Store에 보드 데이터 설정
        useDashboardStore.setState({ 
          boards: result.data || [],
          isLoadingBoards: false,
          boardsError: null
        });
        
        // 통계 업데이트
        const boards = result.data || [];
        const totalBoards = boards.length;
        const totalCards = boards.reduce((sum) => sum + 0, 0); // cardCount 속성이 없으므로 기본값 0 사용
        const favoriteBoards = boards.filter(board => (board as any).isFavorite).length;
        const archivedBoards = boards.filter(board => (board as any).isArchived).length;

        useDashboardStore.setState({
          stats: {
            totalBoards,
            totalCards,
            favoriteBoards,
            archivedBoards,
          }
        });
        
        console.log("보드 목록:", result.data);
      }
    } catch (error) {
      console.error("보드 목록 조회 실패:", error);
      useDashboardStore.setState({ 
        boardsError: '보드 목록을 불러오는데 실패했습니다.',
        isLoadingBoards: false 
      });
    }
  }, [authenticated]);

  // 전체 대시보드 데이터 로딩
  const handleLoadDashboardData = useCallback(async () => {
    if (!authenticated) {
      console.log("인증되지 않은 상태입니다.");
      return;
    }

    // 로딩 상태 설정
    useDashboardStore.setState({
      isLoadingBoards: true,
      isLoadingStats: true,
      isLoadingActivity: true,
      boardsError: null,
      statsError: null,
      activityError: null
    });

    try {
      // 보드 목록 로딩
      await handleLoadBoards();
      
      // 최근 활동 로딩 (현재는 더미 데이터)
      const dummyActivity = [
        {
          id: "activity_9c8b7a6d",
          type: "CARD_MOVE",
          actor: {
            id: "user_101",
            firstName: "개발",
            lastName: "김",
            profileImageUrl: "https://placehold.co/40x40/0284C7/FFFFFF?text=김"
          },
          timestamp: "2025-01-20T15:30:00.123Z",
          payload: {
            cardTitle: "API 설계",
            sourceListName: "진행 중",
            destListName: "완료",
            cardId: "card_456",
            sourceListId: "list_123",
            destListId: "list_789"
          }
        },
        {
          id: "activity_8b7a6c5d",
          type: "CARD_CREATE",
          actor: {
            id: "user_101",
            firstName: "개발",
            lastName: "김",
            profileImageUrl: "https://placehold.co/40x40/0284C7/FFFFFF?text=김"
          },
          timestamp: "2025-01-20T12:45:00.456Z",
          payload: {
            listName: "할 일",
            cardTitle: "사용자 인증 구현",
            listId: "list_123",
            cardId: "card_789"
          }
        },
        {
          id: "activity_7a6b5c4d",
          type: "BOARD_CREATE",
          actor: {
            id: "user_101",
            firstName: "개발",
            lastName: "김",
            profileImageUrl: "https://placehold.co/40x40/0284C7/FFFFFF?text=김"
          },
          timestamp: "2025-01-19T09:15:00.789Z",
          payload: {
            boardName: "독서 계획",
            boardId: "board_4"
          }
        }
      ];
      
      useDashboardStore.setState({
        recentActivity: dummyActivity,
        isLoadingActivity: false
      });
      
    } catch (error) {
      console.error("대시보드 데이터 로딩 실패:", error);
    }
  }, [authenticated, handleLoadBoards]);

  // 새 보드 생성 성공 후 처리
  const handleCreateBoardSuccess = useCallback(() => {
    handleLoadBoards();
  }, [handleLoadBoards]);

  // 보드 업데이트
  const handleUpdateBoard = useCallback((boardId: string, updates: Partial<apiClient.BoardResponse>) => {
    updateBoard(boardId, updates);
  }, [updateBoard]);

  // 보드 삭제
  const handleRemoveBoard = useCallback((boardId: string) => {
    removeBoard(boardId);
  }, [removeBoard]);

  // 보드 보관
  const handleArchiveBoard = useCallback((boardId: string) => {
    archiveBoard(boardId);
  }, [archiveBoard]);

  // 검색 쿼리 설정
  const handleSetSearchQuery = useCallback((query: string) => {
    setSearchQuery(query);
  }, [setSearchQuery]);

  // 보관된 보드 포함 여부 설정
  const handleSetIncludeArchived = useCallback((include: boolean) => {
    setIncludeArchived(include);
    handleLoadBoards(include);
  }, [setIncludeArchived, handleLoadBoards]);

  // 에러 초기화
  const handleResetErrors = useCallback(() => {
    resetErrors();
  }, [resetErrors]);

  // 데이터 초기화
  const handleClearData = useCallback(() => {
    clearData();
  }, [clearData]);

  return {
    // 데이터 로딩
    loadBoards: handleLoadBoards,
    loadDashboardData: handleLoadDashboardData,
    handleCreateBoardSuccess,
    
    // 데이터 업데이트
    updateBoard: handleUpdateBoard,
    removeBoard: handleRemoveBoard,
    archiveBoard: handleArchiveBoard,
    
    // 필터링 및 검색
    setSearchQuery: handleSetSearchQuery,
    setIncludeArchived: handleSetIncludeArchived,
    
    // 상태 관리
    resetErrors: handleResetErrors,
    clearData: handleClearData,
  };
} 