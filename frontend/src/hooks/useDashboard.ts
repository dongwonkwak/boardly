import { useCallback } from 'react';
import { useApi } from './useApi';
import { useDashboardStore } from '../store/dashboardStore';
import type * as apiClient from '@/services/api/client';

/**
 * 대시보드 데이터 로딩을 위한 커스텀 훅
 * API 호출과 store 업데이트를 처리합니다.
 */
export function useDashboard() {
  const { authenticated } = useApi();
  const {
    setBoards,
    setLoadingBoards,
    setBoardsError,
    setStats,
    setLoadingStats,
    setStatsError,
    setActivities,
    setLoadingActivities,
    setActivitiesError,
    setSearchQuery,
    addBoard,
    updateBoard,
    removeBoard,
    reset
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
        
        // 통계 업데이트 (보드 목록만 가져올 때는 기본값 사용)
        const boards = result.data || [];
        const totalBoards = boards.length;
        const totalCards = 0; // 보드 목록만으로는 카드 수를 알 수 없음
        const starredBoards = boards.filter(board => board.isStarred).length;
        const archivedBoards = boards.filter(board => (board as any).isArchived).length;

        useDashboardStore.setState({
          stats: {
            totalBoards,
            totalCards,
            starredBoards,
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
      isLoadingActivities: true,
      boardsError: null,
      statsError: null,
      activitiesError: null
    });

    try {
      // 대시보드 데이터 로딩 (보드와 액티비티 리스트)
      const result = await (authenticated.getDashboard as typeof apiClient.getDashboard)();
      
      if (result.status === 200) {
        const dashboardData = result.data;
        
        // 보드 데이터 설정
        useDashboardStore.setState({ 
          boards: dashboardData.boards || [],
          isLoadingBoards: false,
          boardsError: null
        });
        
        // 활동 데이터 설정 (DashboardResponse에 activities가 없을 수 있으므로 빈 배열로 처리)
        useDashboardStore.setState({
          activities: (dashboardData as any).recentActivity || [],
          isLoadingActivities: false,
          activitiesError: null
        });
        
        // 통계 데이터 설정 (백엔드에서 전송되는 형식)
        const statistics = (dashboardData as any).statistics || {
          totalBoards: 0,
          totalCards: 0,
          starredBoards: 0,
          archivedBoards: 0
        };

        useDashboardStore.setState({
          stats: {
            totalBoards: statistics.totalBoards,
            totalCards: statistics.totalCards,
            starredBoards: statistics.starredBoards,
            archivedBoards: statistics.archivedBoards,
          },
          isLoadingStats: false,
          statsError: null
        });
        
        console.log("대시보드 데이터:", dashboardData);
      }
    } catch (error) {
      console.error("대시보드 데이터 로딩 실패:", error);
      useDashboardStore.setState({ 
        boardsError: '대시보드 데이터를 불러오는데 실패했습니다.',
        statsError: '통계 데이터를 불러오는데 실패했습니다.',
        activitiesError: '활동 내역을 불러오는데 실패했습니다.',
        isLoadingBoards: false,
        isLoadingStats: false,
        isLoadingActivities: false
      });
    }
  }, [authenticated]);

  // 새 보드 생성 성공 후 처리
  const handleCreateBoardSuccess = useCallback(() => {
    handleLoadDashboardData();
  }, [handleLoadDashboardData]);

  // 보드 업데이트
  const handleUpdateBoard = useCallback((boardId: string, updates: Partial<apiClient.BoardResponse>) => {
    updateBoard(boardId, updates);
  }, [updateBoard]);

  // 보드 삭제
  const handleRemoveBoard = useCallback((boardId: string) => {
    removeBoard(boardId);
  }, [removeBoard]);

  // 검색 쿼리 설정
  const handleSetSearchQuery = useCallback((query: string) => {
    setSearchQuery(query);
  }, [setSearchQuery]);

  // 에러 초기화
  const handleResetErrors = useCallback(() => {
    setBoardsError(null);
    setStatsError(null);
    setActivitiesError(null);
  }, [setBoardsError, setStatsError, setActivitiesError]);

  // 데이터 초기화
  const handleClearData = useCallback(() => {
    reset();
  }, [reset]);

  return {
    // 데이터 로딩
    loadBoards: handleLoadBoards,
    loadDashboardData: handleLoadDashboardData,
    handleCreateBoardSuccess,
    
    // 데이터 업데이트
    updateBoard: handleUpdateBoard,
    removeBoard: handleRemoveBoard,
    
    // 필터링 및 검색
    setSearchQuery: handleSetSearchQuery,
    
    // 상태 관리
    resetErrors: handleResetErrors,
    clearData: handleClearData,
  };
} 