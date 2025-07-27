import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import type * as apiClient from '@/services/api/client';

interface DashboardState {
  // 보드 데이터
  boards: apiClient.BoardSummaryDto[];
  isLoadingBoards: boolean;
  boardsError: string | null;
  
  // 통계 데이터
  stats: apiClient.DashboardStatisticsDto;
  isLoadingStats: boolean;
  statsError: string | null;
  
  // 활동 데이터
  activities: apiClient.ActivityResponse[];
  isLoadingActivities: boolean;
  activitiesError: string | null;
  
  // 검색 및 필터
  searchQuery: string;
  viewMode: 'grid' | 'list';
  
  // 액션들
  setBoards: (boards: apiClient.BoardSummaryDto[]) => void;
  setLoadingBoards: (loading: boolean) => void;
  setBoardsError: (error: string | null) => void;
  
  setStats: (stats: apiClient.DashboardStatisticsDto) => void;
  setLoadingStats: (loading: boolean) => void;
  setStatsError: (error: string | null) => void;
  
  setActivities: (activities: apiClient.ActivityResponse[]) => void;
  setLoadingActivities: (loading: boolean) => void;
  setActivitiesError: (error: string | null) => void;
  
  setSearchQuery: (query: string) => void;
  setViewMode: (mode: 'grid' | 'list') => void;
  
  // 보드 관리 액션들
  addBoard: (board: apiClient.BoardSummaryDto) => void;
  updateBoard: (boardId: string, updates: Partial<apiClient.BoardSummaryDto>) => void;
  removeBoard: (boardId: string) => void;
  
  // 초기화
  reset: () => void;
}

const initialState: {
  boards: apiClient.BoardSummaryDto[];
  isLoadingBoards: boolean;
  boardsError: string | null;
  stats: apiClient.DashboardStatisticsDto;
  isLoadingStats: boolean;
  statsError: string | null;
  activities: apiClient.ActivityResponse[];
  isLoadingActivities: boolean;
  activitiesError: string | null;
  searchQuery: string;
  viewMode: 'grid';
} = {
  boards: [],
  isLoadingBoards: false,
  boardsError: null,
  
  stats: {
    totalBoards: 0,
    totalCards: 0,
    starredBoards: 0,
    archivedBoards: 0,
  },
  isLoadingStats: false,
  statsError: null,
  
  activities: [],
  isLoadingActivities: false,
  activitiesError: null,
  
  searchQuery: '',
  viewMode: 'grid' as const,
};

export const useDashboardStore = create<DashboardState>()(
  devtools(
    (set, get) => ({
      ...initialState,
      
      // 보드 관련 액션들
      setBoards: (boards) => set({ boards }, false, 'dashboard/setBoards'),
      setLoadingBoards: (isLoadingBoards) => set({ isLoadingBoards }, false, 'dashboard/setLoadingBoards'),
      setBoardsError: (boardsError) => set({ boardsError }, false, 'dashboard/setBoardsError'),
      
      // 통계 관련 액션들
      setStats: (stats) => set({ stats }, false, 'dashboard/setStats'),
      setLoadingStats: (isLoadingStats) => set({ isLoadingStats }, false, 'dashboard/setLoadingStats'),
      setStatsError: (statsError) => set({ statsError }, false, 'dashboard/setStatsError'),
      
      // 활동 관련 액션들
      setActivities: (activities) => set({ activities }, false, 'dashboard/setActivities'),
      setLoadingActivities: (isLoadingActivities) => set({ isLoadingActivities }, false, 'dashboard/setLoadingActivities'),
      setActivitiesError: (activitiesError) => set({ activitiesError }, false, 'dashboard/setActivitiesError'),
      
      // UI 상태 액션들
      setSearchQuery: (searchQuery) => set({ searchQuery }, false, 'dashboard/setSearchQuery'),
      setViewMode: (viewMode) => set({ viewMode }, false, 'dashboard/setViewMode'),
      
      // 보드 관리 액션들
      addBoard: (board) => {
        const { boards } = get();
        set({ boards: [board, ...boards] }, false, 'dashboard/addBoard');
      },
      
      updateBoard: (boardId, updates) => {
        const { boards } = get();
        const updatedBoards = boards.map(board => 
          board.id === boardId ? { ...board, ...updates } : board
        );
        set({ boards: updatedBoards }, false, 'dashboard/updateBoard');
      },
      
      removeBoard: (boardId) => {
        const { boards } = get();
        const filteredBoards = boards.filter(board => board.id !== boardId);
        set({ boards: filteredBoards }, false, 'dashboard/removeBoard');
      },
      
      // 초기화
      reset: () => set(initialState, false, 'dashboard/reset'),
    }),
    {
      name: 'dashboard-store',
    }
  )
);

// 선택자 함수들 (성능 최적화를 위한 메모이제이션)
export const useDashboardSelectors = {
  // 필터링된 보드들
  useFilteredBoards: () => {
    const { boards, searchQuery } = useDashboardStore();
    return boards.filter(board =>
      (board.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (board.description || '').toLowerCase().includes(searchQuery.toLowerCase())
    );
  },
  
  // 즐겨찾기된 보드들
  useFavoriteBoards: () => {
    const { boards } = useDashboardStore();
    return boards.filter(board => board.isStarred);
  },
  
  // 보관된 보드들
  useArchivedBoards: () => {
    const { boards } = useDashboardStore();
    return boards.filter(board => (board as any).isArchived);
  },
  
  // 최근 활동들 (최대 10개)
  useRecentActivities: () => {
    const { activities } = useDashboardStore();
    return activities.slice(0, 10);
  },
  
  // 로딩 상태
  useIsLoading: () => {
    const { isLoadingBoards, isLoadingStats, isLoadingActivities } = useDashboardStore();
    return isLoadingBoards || isLoadingStats || isLoadingActivities;
  },
  
  // 에러 상태
  useHasError: () => {
    const { boardsError, statsError, activitiesError } = useDashboardStore();
    return !!(boardsError || statsError || activitiesError);
  },
}; 