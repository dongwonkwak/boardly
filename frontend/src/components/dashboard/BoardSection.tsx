import { Search, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import type * as apiClient from "@/services/api/client";
import { getBoardThemeColor } from "@/lib/utils";
import { useTranslation } from "react-i18next";

type ViewMode = 'grid' | 'list';

interface BoardCardProps {
  board: apiClient.BoardSummaryDto;
  viewMode: ViewMode;
  onToggleStar?: (boardId: string) => void;
}

// 즐겨찾기 버튼 컴포넌트
function StarButton({ 
  isStarred, 
  onToggle, 
  size = 'md' 
}: { 
  isStarred: boolean; 
  onToggle: (e: React.MouseEvent) => void; 
  size?: 'sm' | 'md';
}) {
  const sizeClasses = size === 'sm' ? 'w-4 h-4' : 'w-5 h-5';
  const colorClasses = isStarred 
    ? (size === 'sm' ? 'text-yellow-500' : 'text-yellow-400') 
    : (size === 'sm' ? 'text-gray-300 hover:text-yellow-400' : 'text-white/60 hover:text-yellow-300');

  return (
    <button
      type="button"
      onClick={onToggle}
      className={`${sizeClasses} transition-colors ${colorClasses}`}
      aria-label={isStarred ? '즐겨찾기 해제' : '즐겨찾기 추가'}
    >
      <svg className={sizeClasses} fill={isStarred ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 20 20" aria-hidden="true">
        <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
      </svg>
    </button>
  );
}

// 더보기 버튼 컴포넌트
function MoreButton({ size = 'md' }: { size?: 'sm' | 'md' }) {
  const sizeClasses = size === 'sm' ? 'w-4 h-4' : 'w-5 h-5';
  const colorClasses = size === 'sm' ? 'text-gray-400 hover:text-gray-600' : 'text-white/80 hover:text-white';

  return (
    <button type="button" className={`${colorClasses} opacity-0 group-hover:opacity-100 transition-opacity`} aria-label="더보기">
      <svg className={sizeClasses} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
      </svg>
    </button>
  );
}

function BoardCard({ board, viewMode, onToggleStar }: BoardCardProps) {
  const { t } = useTranslation('common');
  const color = getBoardThemeColor(board.id || '', board.color);

  const handleStarToggle = (e: React.MouseEvent) => {
    e.stopPropagation();
    onToggleStar?.(board.id || '');
  };

  if (viewMode === 'grid') {
    return (
      <div className="group bg-white rounded-xl shadow-sm border border-gray-100 hover:shadow-lg transition-all duration-200 cursor-pointer transform hover:-translate-y-1">
        <div className={`h-32 rounded-t-xl ${color} p-6 relative overflow-hidden`}>
          <div className="absolute inset-0 bg-black/10"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-start">
              <h4 className="text-white font-semibold text-lg truncate pr-4">
                {board.title || t('board.section.noTitle')}
              </h4>
              <MoreButton />
            </div>
            <div className="absolute bottom-4 right-4">
              <StarButton isStarred={board.isStarred || false} onToggle={handleStarToggle} />
            </div>
          </div>
        </div>
        <div className="p-6">
          <p className="text-gray-600 text-sm mb-4 line-clamp-2">
            {board.description || t('board.section.noDescription')}
          </p>
          <div className="flex items-center justify-between text-xs text-gray-500">
            <div className="flex items-center space-x-4">
              <span>{t('board.section.listCount', { count: board.listCount || 0 })}</span>
              <span>{t('board.section.cardCount', { count: board.cardCount || 0 })}</span>
            </div>
            <span>{new Date(board.createdAt || Date.now()).toLocaleDateString()}</span>
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
            {(board.title || t('board.section.noTitle')).charAt(0)}
          </span>
        </div>
        <div>
          <div className="flex items-center space-x-2">
            <h4 className="font-semibold text-gray-900">{board.title || t('board.section.noTitle')}</h4>
            <StarButton isStarred={board.isStarred || false} onToggle={handleStarToggle} size="sm" />
          </div>
          <p className="text-gray-600 text-sm">{board.description || t('board.section.noDescription')}</p>
        </div>
      </div>
      <div className="flex items-center space-x-6 text-sm text-gray-500">
        <span>{t('board.section.listCount', { count: board.listCount || 0 })}</span>
        <span>{t('board.section.cardCount', { count: board.cardCount || 0 })}</span>
        <span>{new Date(board.createdAt || Date.now()).toLocaleDateString()}</span>
        <MoreButton size="sm" />
      </div>
    </div>
  );
}

interface BoardSectionProps {
  boards: apiClient.BoardSummaryDto[];
  viewMode: ViewMode;
  searchQuery: string;
  isLoadingBoards: boolean;
  onOpenCreateModal: () => void;
  authenticated: boolean;
  onToggleStar?: (boardId: string) => void;
}

export function BoardSection({ 
  boards, 
  viewMode, 
  searchQuery, 
  isLoadingBoards, 
  onOpenCreateModal, 
  authenticated,
  onToggleStar
}: BoardSectionProps) {
  const { t } = useTranslation('common');
  const filteredBoards = boards.filter(board =>
    (board.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (board.description || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="mb-8">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-semibold text-gray-900">
          {searchQuery ? t('board.section.searchResults', { query: searchQuery }) : t('board.section.recentBoards')}
        </h3>
        {searchQuery && (
          <span className="text-sm text-gray-500">{t('board.section.resultCount', { count: filteredBoards.length })}</span>
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
            <h3 className="text-lg font-medium text-gray-900 mb-2">{t('board.section.loading.title')}</h3>
            <p className="text-gray-600">{t('board.section.loading.description')}</p>
          </div>
        </div>
      ) : (
        <>
          {viewMode === 'grid' ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredBoards.map((board) => (
                <BoardCard key={board.id || `${board.title}-${board.createdAt}`} board={board} viewMode={viewMode} onToggleStar={onToggleStar} />
              ))}
            </div>
          ) : filteredBoards.length > 0 ? (
            <div className="bg-white rounded-xl shadow-sm border border-gray-100">
              <div className="overflow-hidden">
                {filteredBoards.map((board) => (
                  <BoardCard key={board.id || `${board.title}-${board.createdAt}`} board={board} viewMode={viewMode} onToggleStar={onToggleStar} />
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
            {searchQuery ? t('board.section.empty.noSearchResults') : t('board.section.empty.noBoards')}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchQuery 
              ? t('board.section.empty.noSearchResultsDescription')
              : t('board.section.empty.noBoardsDescription')
            }
          </p>
          {!searchQuery && (
            <Button 
              onClick={onOpenCreateModal} 
              disabled={!authenticated}
              className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4 mr-2" />
              {t('board.section.empty.createFirstBoard')}
            </Button>
          )}
        </div>
      )}
    </div>
  );
} 