# Boardly 프로젝트 문서

Boardly는 칸반 보드 기반의 협업 프로젝트 관리 서비스입니다.

## 📚 자동 Notion 동기화

이 저장소는 GitHub Actions를 통해 `docs/` 폴더의 마크다운 파일 변경사항을 자동으로 Notion에 동기화합니다.

### 🔧 설정 방법

#### 1. Notion 설정

1. **Notion Integration 생성**
   - [Notion Developers](https://developers.notion.com/)에서 새 Integration 생성
   - API 토큰 복사

2. **Notion Database 생성**
   - Notion에서 새 데이터베이스 페이지 생성
   - 다음 속성들을 추가:
     - **Name** (Title): 페이지 제목
     - **File Path** (Text): 파일 경로  
     - **Last Updated** (Date): 마지막 업데이트 시간
   - 페이지를 Integration과 공유
   - Database ID 복사 (URL에서 확인 가능)

#### 2. GitHub Secrets 설정

Repository Settings > Secrets and variables > Actions에서 다음 secrets를 추가:

| Secret 이름 | 설명 | 예시 |
|-------------|------|------|
| `NOTION_API_TOKEN` | Notion Integration API 토큰 | `secret_abc123...` |
| `NOTION_DATABASE_ID` | 생성한 Database의 ID | `a1b2c3d4-e5f6-...` |

#### 3. 첫 번째 동기화

설정 완료 후 docs 폴더의 마크다운 파일을 수정하고 main 또는 develop 브랜치에 push하면 자동으로 동기화가 실행됩니다.

### 🚀 사용법

#### 자동 동기화
- `docs/` 폴더의 `.md` 파일을 수정 후 `main` 또는 `develop` 브랜치에 push
- GitHub Actions가 자동으로 변경된 파일만 Notion에 동기화

#### 수동 동기화
1. Repository의 Actions 탭으로 이동
2. "Notion 문서 동기화" 워크플로우 선택
3. "Run workflow" 버튼 클릭
4. 필요시 "모든 파일 강제 동기화" 옵션 체크

### 📋 동기화 동작

- **새 파일**: Notion에 새 페이지 생성
- **수정된 파일**: 기존 페이지 내용 업데이트
- **기존 미동기화 파일**: 노션에 매핑되지 않은 기존 파일 자동 감지하여 동기화
- **삭제된 파일**: 해당 Notion 페이지는 그대로 유지 (수동 정리 필요)
- **중복 방지**: 파일 해시 비교로 실제 변경사항만 동기화

#### 🎯 스마트 동기화
- **변경된 파일**: Git diff로 변경된 파일 자동 감지
- **누락된 파일**: 노션에 없는 기존 문서 자동 발견하여 함께 동기화
- **첫 설정 시**: 모든 기존 문서가 자동으로 노션에 추가됨

### 🗂️ 프로젝트 구조

```
docs/
├── architecture/         # 시스템 아키텍처
├── design/              # 설계 문서
├── management/          # 프로젝트 관리
├── planning/            # 계획 및 로드맵
└── principles/          # 개발 원칙
```

### 🔍 문제 해결

#### 동기화가 실행되지 않는 경우
1. GitHub Secrets가 올바르게 설정되었는지 확인
2. Notion Integration이 Database에 접근 권한이 있는지 확인
3. Actions 탭에서 워크플로우 실행 로그 확인

#### 동기화 실패하는 경우
1. Actions 탭에서 상세 로그 확인
2. Database ID와 API 토큰이 올바른지 확인
3. Notion Database 속성이 올바르게 설정되었는지 확인

### 📞 문의

프로젝트 관련 문의사항은 GitHub Issues를 통해 남겨주세요.