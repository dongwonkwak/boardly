// 지원하는 언어 목록
export const SUPPORTED_LANGUAGES = {
  ko: '한국어',
  en: 'English',
} as const;

export type SupportedLanguage = keyof typeof SUPPORTED_LANGUAGES;

const LANGUAGE_STORAGE_KEY = 'boardly-language';

// localStorage에서 언어 설정 로드
export const getStoredLanguage = (): SupportedLanguage | null => {
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
export const setStoredLanguage = (language: SupportedLanguage): void => {
  try {
    localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  } catch (error) {
    console.warn('Failed to store language:', error);
  }
};

// Accept-Language 헤더에서 지원하는 언어 감지
export const detectBrowserLanguage = (): SupportedLanguage => {
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
export const getInitialLanguage = (): SupportedLanguage => {
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