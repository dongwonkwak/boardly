# 국제화 (Internationalization) 가이드

## 개요

Boardly는 다국어 지원을 통해 글로벌 사용자에게 서비스를 제공합니다. 현재 한국어와 영어를 지원하며, 향후 추가 언어 확장이 가능하도록 설계되었습니다.

## 🌐 지원 언어

### 현재 지원
- **한국어 (ko)**: 기본 언어
- **영어 (en)**: 보조 언어

### 향후 확장 계획
- 일본어 (ja)
- 중국어 간체 (zh-CN)
- 중국어 번체 (zh-TW)

## 🛠️ 기술 스택

### Frontend
- **라이브러리**: react-i18next
- **번역 파일 형식**: JSON
- **언어 감지**: 브라우저 설정, 사용자 선택
- **폴백**: 한국어 (ko)

### Backend
- **라이브러리**: Spring Boot MessageSource
- **번역 파일 형식**: Properties 또는 YAML
- **API 응답**: Accept-Language 헤더 기반

## 📁 파일 구조

### Frontend 구조
```
src/locales/
├── index.ts                    # i18n 초기화 설정
├── resources.ts                # 리소스 타입 정의
├── ko/                         # 한국어 번역
│   ├── common.json            # 공통 용어
│   ├── auth.json              # 인증 관련
│   ├── workspace.json         # 워크스페이스
│   ├── board.json             # 보드 관리
│   ├── card.json              # 카드 관리
│   ├── comment.json           # 댓글
│   ├── invitation.json        # 초대
│   ├── error.json             # 오류 메시지
│   └── validation.json        # 폼 검증
└── en/                         # 영어 번역
    ├── common.json
    ├── auth.json
    ├── workspace.json
    ├── board.json
    ├── card.json
    ├── comment.json
    ├── invitation.json
    ├── error.json
    └── validation.json
```

### Backend 구조
```
src/main/resources/messages/
├── messages_ko.properties      # 한국어 메시지
├── messages_en.properties      # 영어 메시지
└── validation_ko.properties    # 한국어 검증 메시지
└── validation_en.properties    # 영어 검증 메시지
```

## 🔧 설정 및 구현

### Frontend 설정

#### 1. react-i18next 설정
```typescript
// src/locales/index.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// 번역 리소스 import
import koCommon from './ko/common.json';
import koAuth from './ko/auth.json';
import koWorkspace from './ko/workspace.json';
import koBoardManagement from './ko/board.json';

import enCommon from './en/common.json';
import enAuth from './en/auth.json';
import enWorkspace from './en/workspace.json';
import enBoardManagement from './en/board.json';

const resources = {
  ko: {
    common: koCommon,
    auth: koAuth,
    workspace: koWorkspace,
    board: koBoardManagement,
  },
  en: {
    common: enCommon,
    auth: enAuth,
    workspace: enWorkspace,
    board: enBoardManagement,
  },
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'ko',
    defaultNS: 'common',
    
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag'],
      caches: ['localStorage'],
    },

    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
```

#### 2. 컴포넌트에서 사용
```typescript
// 예시: 워크스페이스 생성 컴포넌트
import { useTranslation } from 'react-i18next';

export const CreateWorkspaceForm = () => {
  const { t } = useTranslation(['workspace', 'common']);

  return (
    <form>
      <h2>{t('workspace:create.title')}</h2>
      <input 
        placeholder={t('workspace:create.name.placeholder')} 
        aria-label={t('workspace:create.name.label')}
      />
      <button type="submit">
        {t('common:actions.create')}
      </button>
    </form>
  );
};
```

#### 3. 언어 변경 컴포넌트
```typescript
// 언어 선택 드롭다운
import { useTranslation } from 'react-i18next';

export const LanguageSelector = () => {
  const { i18n, t } = useTranslation();

  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
  };

  return (
    <select 
      value={i18n.language} 
      onChange={(e) => changeLanguage(e.target.value)}
    >
      <option value="ko">{t('common:languages.korean')}</option>
      <option value="en">{t('common:languages.english')}</option>
    </select>
  );
};
```

### Backend 설정

#### 1. Spring Boot 메시지 설정
```java
// MessageSourceConfig.java
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource 
            = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
}
```

#### 2. 컨트롤러에서 사용
```java
// 예시: 워크스페이스 컨트롤러
@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    @Autowired
    private MessageSource messageSource;

    @PostMapping
    public ResponseEntity<?> createWorkspace(
            @RequestBody CreateWorkspaceRequest request,
            Locale locale) {
        
        try {
            // 워크스페이스 생성 로직
            Workspace workspace = workspaceService.create(request);
            
            String message = messageSource.getMessage(
                "workspace.created.success", 
                new Object[]{workspace.getName()}, 
                locale
            );
            
            return ResponseEntity.ok(new ApiResponse(true, workspace, message));
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage(
                "workspace.created.error", 
                null, 
                locale
            );
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, null, errorMessage));
        }
    }
}
```

## 📝 번역 키 명명 규칙

### 계층적 구조
```json
{
  "namespace": {
    "feature": {
      "action": {
        "field": "번역 텍스트"
      }
    }
  }
}
```

### 예시
```json
// workspace.json
{
  "create": {
    "title": "새 워크스페이스 만들기",
    "name": {
      "label": "워크스페이스 이름",
      "placeholder": "워크스페이스 이름을 입력하세요"
    },
    "description": {
      "label": "설명",
      "placeholder": "워크스페이스에 대한 설명을 입력하세요"
    }
  },
  "edit": {
    "title": "워크스페이스 수정",
    "save": "변경사항 저장"
  },
  "delete": {
    "confirm": "정말로 이 워크스페이스를 삭제하시겠습니까?",
    "warning": "삭제된 워크스페이스는 복구할 수 없습니다."
  }
}
```

## 🎯 번역 가이드라인

### 일관성 유지
1. **용어집 활용**: 핵심 용어는 일관되게 번역
2. **톤앤매너**: 친근하고 전문적인 톤 유지
3. **문맥 고려**: UI 컨텍스트에 맞는 번역

### 핵심 용어 번역
| 한국어 | 영어 | 비고 |
|--------|------|------|
| 워크스페이스 | Workspace | |
| 보드 | Board | |
| 카드 | Card | |
| 컬럼 | Column | |
| 담당자 | Assignee | |
| 라벨 | Label | |
| 댓글 | Comment | |
| 초대 | Invitation | |
| 권한 | Permission | |
| 소유자 | Owner | |

### 문장 구조
- **한국어**: 존댓말 사용, 명확하고 간결한 표현
- **영어**: 명령형 또는 평서문, 간결한 표현

## 🧪 테스트 전략

### 1. 번역 키 누락 테스트
```typescript
// 모든 번역 키가 존재하는지 확인
describe('Translation Keys', () => {
  test('should have all required keys in all languages', () => {
    const koKeys = getTranslationKeys('ko');
    const enKeys = getTranslationKeys('en');
    
    expect(koKeys).toEqual(enKeys);
  });
});
```

### 2. 시각적 테스트
- 다양한 언어에서 UI 레이아웃 확인
- 긴 텍스트로 인한 레이아웃 깨짐 방지

### 3. 언어 변경 테스트
```typescript
// 언어 변경 시 UI 업데이트 확인
describe('Language Switching', () => {
  test('should update UI when language changes', () => {
    render(<App />);
    
    // 한국어 텍스트 확인
    expect(screen.getByText('새 워크스페이스')).toBeInTheDocument();
    
    // 영어로 변경
    changeLanguage('en');
    
    // 영어 텍스트 확인
    expect(screen.getByText('New Workspace')).toBeInTheDocument();
  });
});
```

## 🚀 배포 및 관리

### 1. 번역 파일 관리
- Git으로 버전 관리
- 번역 변경 시 PR 리뷰 프로세스
- 번역 품질 검토

### 2. 자동화
```yaml
# GitHub Actions로 번역 키 일관성 검사
- name: Check Translation Keys
  run: |
    npm run i18n:check
    npm run i18n:validate
```

### 3. 모니터링
- 누락된 번역 키 로깅
- 언어별 사용 통계 수집
- 번역 품질 피드백 수집

## 📈 향후 확장 계획

### 1. 추가 언어 지원
- 아시아 언어 우선 (일본어, 중국어)
- 사용자 요청 기반 언어 추가

### 2. 고급 기능
- 지역별 날짜/시간 형식
- 숫자 및 통화 형식
- RTL (오른쪽에서 왼쪽) 언어 지원

### 3. 관리 도구
- 번역 관리 대시보드
- 번역자 협업 도구 연동
- 자동 번역 품질 검증

이 가이드를 통해 Boardly의 국제화를 체계적으로 구현하고 관리할 수 있습니다.
