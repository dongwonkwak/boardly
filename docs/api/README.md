# Boardly API Documentation

## OpenAPI 사양서

- **JSON 형식**: `openapi.json`
- **YAML 형식**: `openapi.yml` (곧 제공 예정)
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 클라이언트 코드 생성

다음 도구들을 사용하여 클라이언트 코드를 생성할 수 있습니다:

### OpenAPI Generator
```bash
# TypeScript 클라이언트 생성
npx @openapitools/openapi-generator-cli generate \
  -i openapi.json \
  -g typescript-axios \
  -o ./generated-client

# JavaScript 클라이언트 생성  
npx @openapitools/openapi-generator-cli generate \
  -i openapi.json \
  -g javascript \
  -o ./generated-client
```

### Swagger Codegen
```bash
# React Query 클라이언트 생성
npx swagger-codegen-cli generate \
  -i openapi.json \
  -l typescript-fetch \
  -o ./generated-client
```

## 업데이트 주기

이 문서는 백엔드 빌드 시마다 자동으로 업데이트됩니다.
최신 버전을 사용하고 있는지 확인하세요.

마지막 업데이트: Wed Aug 06 17:47:07 KST 2025
