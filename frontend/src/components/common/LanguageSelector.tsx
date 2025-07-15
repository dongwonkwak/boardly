import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { ChevronDown, Globe } from 'lucide-react';
import { 
  SUPPORTED_LANGUAGES, 
  setStoredLanguage,
  getInitialLanguage 
} from '@/utils/languageUtils';
import type { SupportedLanguage } from '@/utils/languageUtils';

export default function LanguageSelector() {
  const { i18n } = useTranslation();
  const [currentLanguage, setCurrentLanguage] = useState<SupportedLanguage>('en');
  const [isOpen, setIsOpen] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);

  // 초기 언어 설정
  useEffect(() => {
    const initializeLanguage = async () => {
      try {
        const initialLanguage = getInitialLanguage();
        setCurrentLanguage(initialLanguage);
        console.log('initialLanguage', initialLanguage);
        await i18n.changeLanguage(initialLanguage);
        setIsInitialized(true);
      } catch (error) {
        console.error('Failed to initialize language:', error);
        setCurrentLanguage('en');
        await i18n.changeLanguage('en');
        setIsInitialized(true);
      }
    };

    initializeLanguage();
  }, [i18n]);

  // 언어 변경 핸들러
  const handleLanguageChange = async (language: SupportedLanguage) => {
    try {
      setCurrentLanguage(language);
      setStoredLanguage(language);
      await i18n.changeLanguage(language);
      setIsOpen(false);
    } catch (error) {
      console.error('Failed to change language:', error);
    }
  };

  // 초기화되지 않았으면 로딩 표시
  if (!isInitialized) {
    return (
      <div className="w-8 h-8 rounded-full bg-gray-200 animate-pulse"></div>
    );
  }

  return (
    <div className="relative">
      {/* 언어 선택 버튼 */}
      <Button
        variant="ghost"
        size="sm"
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900"
        onClick={() => setIsOpen(!isOpen)}
      >
        <Globe className="h-4 w-4" />
        <span className="hidden sm:inline text-sm">
          {SUPPORTED_LANGUAGES[currentLanguage]}
        </span>
        <ChevronDown className={`h-3 w-3 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </Button>

      {/* 드랍다운 메뉴 */}
      {isOpen && (
        <>
          {/* 배경 오버레이 */}
          <div
            role="button"
            tabIndex={0}
            className="fixed inset-0 z-10" 
            onClick={() => setIsOpen(false)}
            onKeyDown={(e) => {
              if (e.key === 'Escape' || e.key === 'Enter' || e.key === ' ') {
                setIsOpen(false);
              }
            }}
          />
          
          {/* 드랍다운 컨텐츠 */}
          <div className="absolute right-0 top-full mt-2 z-20 bg-white border border-gray-200 rounded-lg shadow-lg py-1 min-w-[120px]">
            {Object.entries(SUPPORTED_LANGUAGES).map(([code, name]) => (
              <button
                type="button"
                key={code}
                className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 transition-colors ${
                  currentLanguage === code ? 'bg-blue-50 text-blue-600' : 'text-gray-700'
                }`}
                onClick={() => handleLanguageChange(code as SupportedLanguage)}
              >
                {name}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
} 