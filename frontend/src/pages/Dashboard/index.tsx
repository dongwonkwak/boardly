import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";
import { useOAuth } from "@/hooks/useAuth";
import { useApi } from "@/hooks/useApi";
import { Button } from "@/components/ui/button";
import { useEffect, useState, useMemo } from "react";
import { Activity } from "@/components/dashboard/Activity";
import { QuickActions } from "@/components/dashboard/QuickActions";
import { ProfileCard } from "@/components/dashboard/ProfileCard";
import { CreateBoardModal } from "@/components/dashboard/CreateBoardModal";
import { StatsCards } from "@/components/dashboard/StatsCards";
import { PageHeader } from "@/components/dashboard/PageHeader";
import { DashboardHeader } from "@/components/dashboard/DashboardHeader";
import { 
  Plus,
  MoreVertical, 
  Activity as ActivityIcon,
  Search,
  Star
} from "lucide-react";
import type * as apiClient from "@/services/api/client";
import { BoardSection } from "@/components/dashboard/BoardSection";

type ViewMode = 'grid' | 'list';

interface BoardCardProps {
  board: apiClient.BoardResponse;
  viewMode: ViewMode;
}

function BoardCard({ board, viewMode }: BoardCardProps) {
  const colors = [
    'bg-gradient-to-br from-blue-500 to-purple-600',
    'bg-gradient-to-br from-green-500 to-teal-600',
    'bg-gradient-to-br from-orange-500 to-red-600',
    'bg-gradient-to-br from-purple-500 to-pink-600',
    'bg-gradient-to-br from-indigo-500 to-blue-600',
    'bg-gradient-to-br from-pink-500 to-rose-600'
  ];
  
  const colorIndex = (Number(board.boardId) || 0) % colors.length;
  const color = colors[colorIndex];

  if (viewMode === 'grid') {
    return (
      <div className="group bg-white rounded-xl shadow-sm border border-gray-100 hover:shadow-lg transition-all duration-200 cursor-pointer transform hover:-translate-y-1">
        <div className={`h-32 rounded-t-xl ${color} p-6 relative overflow-hidden`}>
          <div className="absolute inset-0 bg-black/10"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-start">
              <h4 className="text-white font-semibold text-lg truncate pr-4">
                {board.title || '제목 없음'}
              </h4>
              <button type="button" className="text-white/80 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity">
                <MoreVertical className="w-5 h-5" />
              </button>
            </div>
            <div className="absolute bottom-4 right-4">
              <Star className="w-5 h-5 text-yellow-300" />
            </div>
          </div>
        </div>
        <div className="p-6">
          <p className="text-gray-600 text-sm mb-4 line-clamp-2">
            {board.description || '설명이 없습니다'}
          </p>
          <div className="flex items-center justify-between text-xs text-gray-500">
            <div className="flex items-center space-x-4">
              <span>4개 리스트</span>
              <span>12개 카드</span>
            </div>
            <span>{new Date(board.updatedAt || board.createdAt || Date.now()).toLocaleDateString()}</span>
          </div>
        </div>
      </div>
    );
  }

  // List view
  return (
    <div className="flex items-center justify-between p-6 hover:bg-gray-50 cursor-pointer transition-colors border-b border-gray-100 last:border-b-0">
      <div className="flex items-center space-x-4">
        <div className={`w-12 h-12 rounded-lg ${color} flex items-center justify-center`}>
          <span className="text-white font-bold">
            {(board.title || '제목 없음').charAt(0)}
          </span>
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <h4 className="font-semibold text-gray-900">{board.title || '제목 없음'}</h4>
            <Star className="w-4 h-4 text-yellow-500" />
          </div>
          <p className="text-gray-600 text-sm">{board.description || '설명이 없습니다'}</p>
        </div>
      </div>
      <div className="flex items-center space-x-6 text-sm text-gray-500">
        <span>4개 리스트</span>
        <span>12개 카드</span>
        <span>{new Date(board.updatedAt || board.createdAt || Date.now()).toLocaleDateString()}</span>
        <button type="button" className="text-gray-400 hover:text-gray-600">
          <MoreVertical className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
}

export default function Dashboard() {
  const { user: oauthUser } = useOAuth();
  const { authenticated } = useApi();
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [boards, setBoards] = useState<any[]>([]);
  const [isLoadingBoards, setIsLoadingBoards] = useState(false);

  // 더미 활동 데이터
  const dummyActivities = [
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



  const openCreateModal = () => {
    setIsCreateModalOpen(true);
  };

  const closeCreateModal = () => {
    setIsCreateModalOpen(false);
  };



  return (
    <ProtectedRoute>
      <ErrorBoundary>
        <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
        {/* Header */}
        <DashboardHeader
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          viewMode={viewMode}
          setViewMode={setViewMode}
        />

        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col lg:flex-row gap-8">
            {/* Main Content */}
            <div className="flex-1">
              {/* Page Header */}
              <PageHeader
                title="내 보드"
                description="프로젝트와 작업을 체계적으로 관리하세요"
                buttonText="새 보드 만들기"
                onButtonClick={openCreateModal}
                buttonDisabled={!authenticated}
              />

              {/* Stats Cards */}
              <StatsCards />

              {/* Boards Section */}
              <BoardSection
                boards={boards}
                viewMode={viewMode}
                searchQuery={searchQuery}
                isLoadingBoards={isLoadingBoards}
                onOpenCreateModal={openCreateModal}
                authenticated={!!authenticated}
              />
            </div>

            {/* Sidebar */}
            <div className="w-full lg:w-80 space-y-6">
              <ProfileCard 
                user={{
                  name: oauthUser?.profile?.name,
                  givenName: oauthUser?.profile?.given_name,
                  familyName: oauthUser?.profile?.family_name,
                  email: oauthUser?.profile?.email
                }}
              />
              
              <Activity activities={dummyActivities} />
              
              <QuickActions 
                actions={[
                  {
                    id: 'create-board',
                    label: '새 보드 만들기',
                    icon: <Plus className="w-4 h-4 mr-3 text-blue-600" />,
                    onClick: openCreateModal,
                    disabled: !authenticated
                  }
                ]}
              />
            </div>
          </div>
        </div>
      </div>

      {/* Create Board Modal */}
      <CreateBoardModal
        isOpen={isCreateModalOpen}
        onClose={closeCreateModal}
        createBoard={authenticated?.createBoard as any}
        onSuccess={() => {
          // 보드 생성 성공 후 처리
          closeCreateModal();
        }}
      />
        </ErrorBoundary>
    </ProtectedRoute>
  );
} 