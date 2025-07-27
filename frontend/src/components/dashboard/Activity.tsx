import { Button } from "@/components/ui/button";
import { useTranslation } from "react-i18next";
import { useEffect } from "react";
import type { ActivityResponse } from "@/services/api/client";
import { useLanguageStore } from "@/store/languageStore";
import { formatTimeAgo } from "@/utils/formatDate";

interface ActivityProps {
  activities?: ActivityResponse[];
  showViewAll?: boolean;
  maxItems?: number;
}

export function Activity({ 
  activities = [], 
  showViewAll = true, 
  maxItems = 3 
}: ActivityProps) {
  const { t } = useTranslation('activity');
  const { initializeLanguage, isInitialized, currentLanguage } = useLanguageStore();
  const displayActivities = activities.slice(0, maxItems);

  // 컴포넌트 마운트 시 언어 초기화
  useEffect(() => {
    if (!isInitialized) {
      initializeLanguage();
    }
  }, [isInitialized, initializeLanguage]);

  const getActivityMessage = (activity: ActivityResponse): string => {
    const { type, payload, actor } = activity;
    
    // payload에 actor 정보 추가
    const translationParams = {
      ...payload,
      actorFirstName: actor?.firstName,
      actorLastName: actor?.lastName,
    };
    
    return t(type || '', translationParams);
  };

  // 현재 언어에 따른 locale 설정
  const getLocale = (): string => {
    switch (currentLanguage) {
      case 'ko':
        return 'ko-KR';
      case 'en':
        return 'en-US';
      default:
        return 'en-US';
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="font-semibold text-gray-900 mb-4">최근 활동</h3>
      
      {displayActivities.length === 0 ? (
        <div className="text-center py-8">
          <div className="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3">
            <span className="text-gray-400 text-lg">📝</span>
          </div>
          <p className="text-sm text-gray-500">아직 활동 내역이 없습니다</p>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {displayActivities.map((activity) => {
              const message = getActivityMessage(activity);
              const timeAgo = formatTimeAgo(activity.timestamp || '', getLocale());
              
              return (
                <div key={activity.id} className="flex space-x-3">
                  <div className="flex-shrink-0">
                    <img 
                      src={activity.actor?.profileImageUrl} 
                      alt={`${activity.actor?.firstName} ${activity.actor?.lastName}`}
                      className="w-8 h-8 rounded-full"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900">{message}</p>
                    <div className="flex items-center justify-between mt-1">
                      <span className="text-xs text-blue-600">{activity.boardName || '알 수 없음'}</span>
                      <span className="text-xs text-gray-500">{timeAgo}</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          
          {showViewAll && activities.length > maxItems && (
            <Button 
              variant="ghost" 
              className="w-full mt-4 text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              모든 활동 보기
            </Button>
          )}
        </>
      )}
    </div>
  );
} 