# Boardly API 문서

## 개요

이 문서는 Boardly 프로젝트의 **API 설계 문서**입니다. 개발 전 단계에서 백엔드와 프론트엔드 팀 간의 API 인터페이스를 정의하고 합의하는 데 사용됩니다.

> **⚠️ 중요**: 이 문서는 설계 단계의 문서입니다. 실제 개발 시에는 백엔드에서 자동 생성되는 OpenAPI 문서를 사용하시기 바랍니다.

## 파일 구조

```
docs/api/
├── openapi.yaml              # 메인 OpenAPI 스펙 파일
├── paths/                    # API 엔드포인트 정의
│   ├── auth.yaml            # 인증 관련 API
│   ├── users.yaml           # 사용자 관리 API
│   ├── workspaces.yaml      # 워크스페이스 관리 API
│   ├── boards.yaml          # 보드 관리 API
│   ├── cards.yaml           # 카드 관리 API
│   ├── columns.yaml         # 컬럼 관리 API
│   ├── comments.yaml        # 댓글 관리 API
│   ├── labels.yaml          # 라벨 관리 API
│   └── invitations.yaml     # 초대 관리 API
├── schemas/                  # 데이터 스키마 정의
│   └── index.yaml           # 공통 스키마
├── parameters/               # 공통 파라미터 정의
│   └── index.yaml           # 공통 파라미터
├── responses/                # 공통 응답 정의
│   └── index.yaml           # 공통 응답
└── README.md                # 이 파일
```

## 주요 기능

### 1. 인증 (Authentication)
- OAuth2 Authorization Code flow with PKCE
- JWT 토큰 기반 인증
- 토큰 갱신 및 로그아웃

### 2. 워크스페이스 관리
- 워크스페이스 생성/수정/삭제
- 멤버 초대 및 관리
- 권한 기반 접근 제어

### 3. 보드 관리
- 보드 생성/수정/삭제
- 공개/비공개 설정
- 컬럼 및 카드 관리

### 4. 카드 관리
- 카드 생성/수정/삭제
- 카드 이동 및 순서 변경
- 담당자 할당

### 5. 협업 기능
- 댓글 작성/수정/삭제
- 라벨 관리
- 실시간 업데이트

### 6. 초대 시스템
- 외부 사용자 초대
- 초대 링크 생성
- 초대 수락/거부

## 권한 체계

### 워크스페이스 권한
- **OWNER**: 워크스페이스 소유자, 모든 권한 보유
- **ADMIN**: 워크스페이스 관리자, 모든 권한 보유
- **MEMBER**: 일반 멤버, 제한된 권한

### 보드 권한
- **ADMIN**: 보드 관리자, 모든 권한 보유
- **MEMBER**: 편집자, 카드 및 댓글 관리
- **VIEWER**: 조회자, 읽기 전용 권한

## 백엔드 구현 가이드

### 1. OpenAPI 문서 자동 생성

#### Node.js/Express 예시
```javascript
const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');

// OpenAPI 스펙 로드
const openApiSpec = YAML.load('./docs/api/openapi.yaml');

// Swagger UI 설정
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(openApiSpec));

// OpenAPI JSON 엔드포인트
app.get('/openapi.json', (req, res) => {
  res.json(openApiSpec);
});
```

#### Spring Boot 예시
```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Boardly API")
                .version("1.0.0")
                .description("Boardly 프로젝트 관리 서비스 API"));
    }
}
```

### 2. 코드 주석 기반 문서 생성

#### JSDoc 예시
```javascript
/**
 * @swagger
 * /workspaces/{workspaceId}/boards:
 *   post:
 *     tags:
 *       - Boards
 *     summary: 새로운 보드 생성
 *     parameters:
 *       - name: workspaceId
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - title
 *             properties:
 *               title:
 *                 type: string
 *                 minLength: 1
 *                 maxLength: 100
 */
app.post('/workspaces/:workspaceId/boards', createBoard);
```

### 3. 자동화 스크립트

```bash
#!/bin/bash
# generate-api-docs.sh

# OpenAPI 스펙 검증
npx swagger-cli validate docs/api/openapi.yaml

# HTML 문서 생성
npx redoc-cli bundle docs/api/openapi.yaml -o docs/api/index.html

# JSON 형태로 변환
npx swagger-cli bundle docs/api/openapi.yaml -o docs/api/openapi.json

echo "API 문서 생성 완료!"
```

## 프론트엔드 구현 가이드

### 1. API 클라이언트 자동 생성

#### OpenAPI Generator 사용
```bash
# TypeScript 클라이언트 생성
npx @openapitools/openapi-generator-cli generate \
  -i docs/api/openapi.yaml \
  -g typescript-fetch \
  -o src/api/client \
  --additional-properties=supportsES6=true,npmName=@boardly/api-client

# React Query 훅 생성
npx @openapitools/openapi-generator-cli generate \
  -i docs/api/openapi.yaml \
  -g typescript-react-query \
  -o src/api/hooks \
  --additional-properties=supportsES6=true
```

#### Swagger Codegen 사용
```bash
# TypeScript 클라이언트 생성
npx swagger-codegen generate \
  -i docs/api/openapi.yaml \
  -l typescript-fetch \
  -o src/api/client
```

### 2. 생성된 클라이언트 사용 예시

```typescript
import { BoardlyApi } from './api/client';

const api = new BoardlyApi({
  basePath: 'https://api.boardly.com/v1',
  accessToken: 'your-access-token'
});

// 보드 생성
const createBoard = async (workspaceId: string, boardData: CreateBoardRequest) => {
  try {
    const response = await api.createBoard(workspaceId, boardData);
    return response.data;
  } catch (error) {
    console.error('보드 생성 실패:', error);
    throw error;
  }
};

// 워크스페이스 목록 조회
const getWorkspaces = async () => {
  try {
    const response = await api.getWorkspaces();
    return response.data;
  } catch (error) {
    console.error('워크스페이스 조회 실패:', error);
    throw error;
  }
};
```

### 3. React Query 훅 사용 예시

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useGetWorkspaces, useCreateBoard } from './api/hooks';

// 워크스페이스 목록 조회
const WorkspaceList = () => {
  const { data: workspaces, isLoading, error } = useGetWorkspaces();

  if (isLoading) return <div>로딩 중...</div>;
  if (error) return <div>오류가 발생했습니다</div>;

  return (
    <div>
      {workspaces?.data.map(workspace => (
        <div key={workspace.id}>{workspace.name}</div>
      ))}
    </div>
  );
};

// 보드 생성
const CreateBoardForm = ({ workspaceId }: { workspaceId: string }) => {
  const queryClient = useQueryClient();
  const createBoardMutation = useCreateBoard();

  const handleSubmit = (boardData: CreateBoardRequest) => {
    createBoardMutation.mutate(
      { workspaceId, boardData },
      {
        onSuccess: () => {
          // 캐시 무효화
          queryClient.invalidateQueries(['boards', workspaceId]);
        }
      }
    );
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* 폼 내용 */}
    </form>
  );
};
```

## 개발 워크플로우

### 1. 설계 단계 (현재 문서 사용)
1. 요구사항 분석 및 도메인 모델 정의
2. OpenAPI 스펙 작성 (`docs/api/openapi.yaml`)
3. API 엔드포인트별 세부 정의 (`docs/api/paths/`)
4. 백엔드/프론트엔드 팀 간 API 인터페이스 합의
5. 프론트엔드에서 목 데이터 기반 초기 개발

### 2. 백엔드 개발 (자동 생성 문서로 전환)
1. 설계된 API 스펙을 기반으로 구현
2. 코드 주석으로 OpenAPI 스펙 자동 생성
3. Swagger/OpenAPI 도구로 실시간 문서 생성
4. API 테스트 및 검증

### 3. 프론트엔드 개발 (자동 생성 문서 사용)
1. 백엔드 자동 생성 OpenAPI 스펙으로 클라이언트 코드 생성
2. 생성된 클라이언트를 사용하여 실제 API 연동
3. React Query 등으로 상태 관리
4. UI 컴포넌트 구현

### 4. 지속적 통합
1. 백엔드 코드 변경 시 자동으로 문서 업데이트
2. 프론트엔드 클라이언트 자동 재생성
3. API 버전 관리 및 호환성 검증

## 📋 문서 전환 시점

**현재 설계 문서 → 자동 생성 문서**로 전환하는 시점:

1. **백엔드 API 구현 완료** 후
2. **자동 생성 문서의 품질이 충분할 때**
3. **팀 간 API 인터페이스 합의 완료** 후

## 🔄 마이그레이션 체크리스트

- [ ] 백엔드에서 OpenAPI 자동 생성 설정 완료
- [ ] 자동 생성된 문서의 품질 확인
- [ ] 프론트엔드 팀에 문서 위치 변경 안내
- [ ] 기존 설계 문서를 아카이브로 이동
- [ ] CI/CD 파이프라인에 문서 자동 업데이트 설정

## 모니터링 및 테스트

### 1. API 문서 검증
```bash
# OpenAPI 스펙 문법 검증
npx swagger-cli validate docs/api/openapi.yaml

# 스키마 검증
npx swagger-cli validate --schema docs/api/schemas/index.yaml
```

### 2. API 테스트
```bash
# Postman 컬렉션 생성
npx openapi2postmanv2 -s docs/api/openapi.yaml -o postman-collection.json

# Newman으로 테스트 실행
npx newman run postman-collection.json
```

### 3. 성능 모니터링
- API 응답 시간 측정
- 에러율 모니터링
- 사용량 통계 수집

## 보안 고려사항

### 1. 인증 및 인가
- OAuth2 PKCE 플로우 사용
- JWT 토큰 만료 시간 설정
- 권한 기반 접근 제어

### 2. 데이터 검증
- 입력값 검증 및 sanitization
- SQL Injection 방지
- XSS 공격 방지

### 3. API 보안
- HTTPS 사용
- Rate Limiting 적용
- CORS 설정

## 결론

이 API 설계문서를 통해 백엔드와 프론트엔드 팀이 효율적으로 협업할 수 있습니다. OpenAPI 스펙을 기반으로 한 자동화된 개발 워크플로우를 구축하여 개발 생산성을 향상시키고 API의 일관성과 안정성을 보장할 수 있습니다.
