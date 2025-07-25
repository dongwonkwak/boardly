import { Search, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import type * as apiClient from "@/services/api/client";

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
              <button type="button" className="text-white/80 hover:text-white opacity-0 group-hover:opacity-100 transition-opacity" aria-label="더보기">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                </svg>
              </button>
            </div>
            <div className="absolute bottom-4 right-4">
              <svg className="w-5 h-5 text-yellow-300" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
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
            <svg className="w-4 h-4 text-yellow-500" fill="currentColor" viewBox="0 0 20 20" aria-hidden="true">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
            </svg>
          </div>
          <p className="text-gray-600 text-sm">{board.description || '설명이 없습니다'}</p>
        </div>
      </div>
      <div className="flex items-center space-x-6 text-sm text-gray-500">
        <span>4개 리스트</span>
        <span>12개 카드</span>
        <span>{new Date(board.updatedAt || board.createdAt || Date.now()).toLocaleDateString()}</span>
        <button type="button" className="text-gray-400 hover:text-gray-600" aria-label="더보기">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
          </svg>
        </button>
      </div>
    </div>
  );
}

interface BoardSectionProps {
  boards: apiClient.BoardResponse[];
  viewMode: ViewMode;
  searchQuery: string;
  isLoadingBoards: boolean;
  onOpenCreateModal: () => void;
  authenticated: boolean;
}

export function BoardSection({ 
  boards, 
  viewMode, 
  searchQuery, 
  isLoadingBoards, 
  onOpenCreateModal, 
  authenticated 
}: BoardSectionProps) {
  const filteredBoards = boards.filter(board =>
    (board.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (board.description || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-semibold text-gray-900">
          {searchQuery ? `"${searchQuery}" 검색 결과` : '최근 보드'}
        </h3>
        {searchQuery && (
          <span className="text-sm text-gray-500">{filteredBoards.length}개 결과</span>
        )}
      </div>

      {isLoadingBoards ? (
        <div className="flex items-center justify-center py-16">
          <div className="text-center">
            <div className="relative mb-4">
              <div className="animate-spin rounded-full h-12 w-12 border-4 border-blue-200 border-t-blue-600 mx-auto"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <svg className="h-6 w-6 text-blue-600 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">보드를 불러오는 중...</h3>
            <p className="text-gray-600">잠시만 기다려주세요</p>
          </div>
        </div>
      ) : (
        <>
          {viewMode === 'grid' ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredBoards.map((board) => (
                <BoardCard key={board.boardId || `${board.title}-${board.createdAt}`} board={board} viewMode={viewMode} />
              ))}
            </div>
          ) : filteredBoards.length > 0 ? (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100">
              <div className="overflow-hidden">
                {filteredBoards.map((board) => (
                  <BoardCard key={board.boardId || `${board.title}-${board.createdAt}`} board={board} viewMode={viewMode} />
                ))}
              </div>
            </div>
          ) : null}
        </>
      )}

      {!isLoadingBoards && filteredBoards.length === 0 && (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Search className="w-8 h-8 text-gray-400" />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            {searchQuery ? '검색 결과가 없습니다' : '보드가 없습니다'}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchQuery 
              ? '다른 검색어로 시도해보세요' 
              : '새로운 보드를 만들어 프로젝트를 시작해보세요'
            }
          </p>
          {!searchQuery && (
            <Button 
              onClick={onOpenCreateModal} 
              disabled={!authenticated}
              className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4 mr-2" />
              첫 번째 보드 만들기
            </Button>
          )}
        </div>
      )}
    </div>
  );
} 