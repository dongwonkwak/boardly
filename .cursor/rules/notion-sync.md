# Notion 동기화 시스템 - Cursor 룰

## 📚 GitHub Actions 기반 Notion 동기화

이 프로젝트는 GitHub Actions를 통해 docs/ 폴더의 마크다운 파일 변경사항을 자동으로 Notion에 동기화합니다.

### 🔄 동기화 동작 원리

1. **트리거**: docs/**/*.md 파일이 main/develop 브랜치에 push될 때 자동 실행
2. **변경 감지**: git diff로 변경된 마크다운 파일만 감지
3. **동기화**: 
   - 새 파일 → Notion 페이지 생성
   - 수정된 파일 → 기존 페이지 업데이트  
   - 해시 비교로 실제 변경사항만 처리
4. **매핑 관리**: .github/scripts/page-mapping.json에 파일-페이지 매핑 자동 저장

### 📁 관련 파일

- `.github/workflows/notion-sync.yml` - GitHub Actions 워크플로우
- `.github/scripts/notion-sync.js` - 동기화 메인 스크립트
- `.github/scripts/page-mapping.json` - 파일-페이지 매핑 데이터

### 🔧 필수 설정

GitHub Repository Secrets:
- `NOTION_API_TOKEN` - Notion Integration API 토큰
- `NOTION_DATABASE_ID` - Notion Database ID

### 📝 문서 작성 시 주의사항

1. **docs/ 폴더 하위의 마크다운 파일만 동기화 대상**
2. **파일명은 kebab-case로 작성** (예: system-architecture.md)
3. **제목은 파일명에서 자동 생성** (System Architecture)
4. **commit 메시지에 문서 변경 내용 명시 권장**

### 🎯 동기화 확인 방법

1. **GitHub Actions 탭에서 워크플로우 실행 상태 확인**
2. **Notion Database에서 새 페이지/업데이트 확인**
3. **실패 시 Actions 로그에서 오류 메시지 확인**

### 🚀 수동 동기화

긴급한 경우 GitHub Actions에서 수동으로 워크플로우 실행 가능:
- Repository > Actions > "Notion 문서 동기화" > "Run workflow"
- "모든 파일 강제 동기화" 옵션으로 전체 재동기화 가능

### ⚠️ 제약사항

- Notion API rate limit (무료 플랜: 분당 3-5회)
- 대용량 파일은 Notion 블록 제한으로 일부만 동기화
- 이미지 파일은 현재 지원하지 않음
- 삭제된 파일의 Notion 페이지는 자동 삭제되지 않음
