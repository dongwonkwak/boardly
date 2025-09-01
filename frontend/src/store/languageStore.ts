import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import i18n from '@/config/i18n';

// 지원하는 언어 목록
export const SUPPORTED_LANGUAGES = {
  ko: '한국어',
  en: 'English',
} as const;

export type SupportedLanguage = keyof typeof SUPPORTED_LANGUAGES;

const LANGUAGE_STORAGE_KEY = 'boardly-language';

interface LanguageState {
  currentLanguage: SupportedLanguage;
  isInitialized: boolean;
  isLoading: boolean;
}

interface LanguageActions {
  initializeLanguage: () => Promise<void>;
  changeLanguage: (language: SupportedLanguage) => Promise<void>;
  setCurrentLanguage: (language: SupportedLanguage) => void;
}

type LanguageStore = LanguageState & LanguageActions;

// localStorage에서 언어 설정 로드
const getStoredLanguage = (): SupportedLanguage | null => {
  try {
    const stored = localStorage.getItem(LANGUAGE_STORAGE_KEY);
    if (stored && Object.keys(SUPPORTED_LANGUAGES).includes(stored)) {
      return stored as SupportedLanguage;
    }
  } catch (error) {
    console.warn('Failed to get stored language:', error);
  }
  return null;
};

// localStorage에 언어 설정 저장
const setStoredLanguage = (language: SupportedLanguage): void => {
  try {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  } catch (error) {
    console.warn('Failed to store language:', error);
  }
};

// Accept-Language 헤더에서 지원하는 언어 감지
const detectBrowserLanguage = (): SupportedLanguage => {
  try {
    // navigator.languages 배열을 우선 확인 (더 정확한 언어 우선순위)
    if (navigator.languages && navigator.languages.length > 0) {
      for (const lang of navigator.languages) {
        const normalizedLang = lang.toLowerCase();
        
        // 한국어 확인 (ko, ko-KR, ko-kr 등)
        if (normalizedLang.startsWith('ko')) {
          return 'ko';
        }
        
        // 영어 확인 (en, en-US, en-us 등)
        if (normalizedLang.startsWith('en')) {
          return 'en';
        }
      }
    }
    
    // navigator.language fallback
    const browserLang = navigator.language.toLowerCase();
    
    // 한국어 확인 (ko, ko-KR 등)
    if (browserLang.startsWith('ko')) {
      return 'ko';
    }
    
    // 기본값으로 영어 반환
    return 'en';
  } catch (error) {
    console.warn('Failed to detect browser language:', error);
    return 'en';
  }
};

// 초기 언어 설정 (우선순위: 저장된 언어 > Accept-Language 헤더)
const getInitialLanguage = (): SupportedLanguage => {
  // 1. 저장된 언어가 있는지 확인
  const storedLanguage = getStoredLanguage();
  if (storedLanguage) {
    return storedLanguage;
  }

  // 2. Accept-Language 헤더에서 언어 감지
  const browserLanguage = detectBrowserLanguage();
  setStoredLanguage(browserLanguage);
  return browserLanguage;
};

export const useLanguageStore = create<LanguageStore>()(
  devtools(
    (set, get) => ({
      // 상태
      currentLanguage: 'en', // 초기값은 영어로 설정 (초기화 시 실제 값으로 변경됨)
      isInitialized: false,
      isLoading: false,

      // 액션
      initializeLanguage: async () => {
        const { isInitialized } = get();
        if (isInitialized) return; // 이미 초기화되었으면 스킵
        
        set({ isLoading: true });
        
        try {
          const initialLanguage = getInitialLanguage();
          await i18n.changeLanguage(initialLanguage);
          set({ 
            currentLanguage: initialLanguage, 
            isInitialized: true, 
            isLoading: false 
          });
        } catch (error) {
          console.error('Failed to initialize language:', error);
          // 실패 시 기본값으로 설정
          await i18n.changeLanguage('en');
          set({ 
            currentLanguage: 'en', 
            isInitialized: true, 
            isLoading: false 
          });
        }
      },

      changeLanguage: async (language: SupportedLanguage) => {
        set({ isLoading: true });
        
        try {
          setStoredLanguage(language);
          await i18n.changeLanguage(language);
          set({ 
            currentLanguage: language, 
            isLoading: false 
          });
        } catch (error) {
          console.error('Failed to change language:', error);
          set({ isLoading: false });
          throw error; // 에러를 다시 던져서 UI에서 처리할 수 있게 함
        }
      },

      setCurrentLanguage: (language: SupportedLanguage) => {
        set({ currentLanguage: language });
      },
    }),
    {
      name: 'language-store',
    }
  )
);

// 편의를 위한 선택자들
export const useCurrentLanguage = () => useLanguageStore((state) => state.currentLanguage);
export const useLanguageInitialized = () => useLanguageStore((state) => state.isInitialized);
export const useLanguageLoading = () => useLanguageStore((state) => state.isLoading); 