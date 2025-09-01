import { Calendar, Star, Archive, Layers } from "lucide-react";
import { useTranslation } from "react-i18next";

interface StatsCardsProps {
  totalBoards?: number;
  totalCards?: number;
  favorites?: number;
  archived?: number;
}

export function StatsCards({ 
  totalBoards = 0, 
  totalCards = 0, 
  favorites = 0, 
  archived = 0 
}: StatsCardsProps) {
  const { t } = useTranslation('common');
  
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{t('stats.totalBoards')}</p>
            <p className="text-2xl font-bold text-gray-900">{totalBoards}</p>
          </div>
          <div className="p-3 bg-blue-100 rounded-lg">
            <Layers className="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{t('stats.totalCards')}</p>
            <p className="text-2xl font-bold text-gray-900">{totalCards}</p>
          </div>
          <div className="p-3 bg-green-100 rounded-lg">
            <Calendar className="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{t('stats.favorites')}</p>
            <p className="text-2xl font-bold text-gray-900">{favorites}</p>
          </div>
          <div className="p-3 bg-yellow-100 rounded-lg">
            <Star className="w-6 h-6 text-yellow-600" />
          </div>
        </div>
      </div>
      
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{t('stats.archived')}</p>
            <p className="text-2xl font-bold text-gray-900">{archived}</p>
          </div>
          <div className="p-3 bg-gray-100 rounded-lg">
            <Archive className="w-6 h-6 text-gray-600" />
          </div>
        </div>
      </div>
    </div>
  );
} 