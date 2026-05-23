# Boardly API Documentation

## OpenAPI 사양서

- **JSON 형식**: `openapi.json`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 클라이언트 코드 생성

프론트엔드는 현재 `oazapfts`로 OpenAPI 클라이언트를 생성합니다.

```bash
cd frontend
pnpm generate-api
```

수동으로 다른 클라이언트를 생성해야 하는 경우 다음 도구를 사용할 수 있습니다.

### OpenAPI Generator
```bash
# TypeScript 클라이언트 생성
npx @openapitools/openapi-generator-cli generate \
  -i docs/api/openapi.json \
  -g typescript-axios \
  -o ./generated-client

# JavaScript 클라이언트 생성
npx @openapitools/openapi-generator-cli generate \
  -i docs/api/openapi.json \
  -g javascript \
  -o ./generated-client
```

### Swagger Codegen
```bash
# React Query 클라이언트 생성
npx swagger-codegen-cli generate \
  -i docs/api/openapi.json \
  -l typescript-fetch \
  -o ./generated-client
```

## 업데이트 주기

현재 `openapi.json`은 백엔드 빌드 시 자동 갱신되지 않습니다. API 계약이 바뀌면 Swagger UI 또는 `/api-docs` 출력 기준으로 `docs/api/openapi.json`을 갱신한 뒤 프론트엔드 클라이언트를 다시 생성해야 합니다.
