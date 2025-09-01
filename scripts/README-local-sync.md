# 로컬 Notion 동기화 가이드

push 없이 로컬에서 바로 Notion 동기화하는 방법들을 안내합니다.

## 🚀 방법 1: GitHub Actions 수동 실행 (추천)

**가장 쉽고 안정적인 방법입니다.**

### 실행 방법:
1. GitHub Repository 접속
2. **Actions** 탭 클릭
3. **"Notion 문서 동기화"** 워크플로우 선택
4. **"Run workflow"** 버튼 클릭
5. 옵션 선택:
   - ☐ 모든 파일 강제 동기화 (체크하면 전체 동기화)
6. **"Run workflow"** 실행

### 장점:
- ✅ 로컬 설정 불필요
- ✅ 안정적인 환경에서 실행
- ✅ 실행 로그 확인 가능
- ✅ 실패 시 재실행 쉬움

## 🔧 방법 2: 로컬 스크립트 실행

### 1단계: 환경 설정

#### 의존성 설치:
```bash
npm install @notionhq/client dotenv
```

#### 환경변수 설정:
```bash
# local.env.example을 .env로 복사
cp local.env.example .env

# .env 파일 편집
NOTION_API_TOKEN=secret_your_token_here
NOTION_DATABASE_ID=your_database_id_here
```

### 2단계: 스크립트 실행

#### 기본 동기화 (변경된 파일 + 새 파일):
```bash
node scripts/local-notion-sync.js
```

#### 모든 파일 강제 동기화:
```bash
node scripts/local-notion-sync.js force
```

#### 특정 파일만 동기화:
```bash
node scripts/local-notion-sync.js file architecture/system-architecture.md
```

#### 도움말:
```bash
node scripts/local-notion-sync.js help
```

### 실행 예시:
```bash
$ node scripts/local-notion-sync.js

🚀 로컬 Notion 동기화를 시작합니다...
📁 Docs 경로: /Users/user/project/boardly/docs
📋 매핑 파일: /Users/user/project/boardly/scripts/notion-mapping.json
🔄 변경된 파일: 2개
📋 새로 추가된 파일: 3개
📚 총 5개 파일을 동기화합니다:
  📄 architecture/system-architecture.md
  📄 design/boardly-schema.md
  📄 planning/roadmap.md
✅ architecture/system-architecture.md: created
✅ design/boardly-schema.md: created
✅ planning/roadmap.md: created
✅ 페이지 매핑 정보가 저장되었습니다.

📊 동기화 완료: 성공 5개, 실패 0개
```

## 🔍 방법 3: GitHub CLI (고급 사용자)

GitHub CLI가 설치되어 있다면:

```bash
# GitHub CLI로 워크플로우 실행
gh workflow run "Notion 문서 동기화"

# 강제 동기화로 실행
gh workflow run "Notion 문서 동기화" -f force_sync=true

# 실행 상태 확인
gh run list --workflow="Notion 문서 동기화"
```

## 📋 비교표

| 방법 | 설정 복잡도 | 안정성 | 로그 확인 | 추천도 |
|------|-------------|--------|-----------|---------|
| GitHub Actions 수동 실행 | ⭐ (쉬움) | ⭐⭐⭐ | ⭐⭐⭐ | 🥇 |
| 로컬 스크립트 | ⭐⭐ (보통) | ⭐⭐ | ⭐⭐ | 🥈 |
| GitHub CLI | ⭐⭐⭐ (어려움) | ⭐⭐⭐ | ⭐⭐⭐ | 🥉 |

## 🎯 권장사항

**일반적인 경우:** GitHub Actions 수동 실행을 추천합니다.
- 설정이 간단하고 안정적
- 웹 브라우저만 있으면 언제든 실행 가능

**개발 중 빈번한 테스트:** 로컬 스크립트를 추천합니다.
- 터미널에서 빠르게 실행 가능
- 특정 파일만 선택적으로 동기화 가능

## 🔧 문제 해결

### 환경변수 오류
```
❌ NOTION_API_TOKEN 환경 변수가 필요합니다.
```
→ `.env` 파일을 생성하고 올바른 토큰을 설정하세요.

### 권한 오류
```
❌ You do not have access to the requested resource
```
→ Notion Integration이 Database에 접근 권한이 있는지 확인하세요.

### 파일 경로 오류
```
⚠️ 파일이 존재하지 않습니다
```
→ docs 폴더 기준 상대 경로로 입력하세요. (예: `architecture/system-architecture.md`)

## 📞 도움이 필요하다면

1. **GitHub Actions 로그 확인**: Actions 탭에서 상세 실행 로그 확인
2. **환경변수 재확인**: 토큰과 Database ID가 올바른지 확인
3. **파일 경로 확인**: docs/ 폴더 구조와 일치하는지 확인
