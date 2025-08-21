#!/bin/bash

# Boardly API 문서 자동 생성 스크립트
# 사용법: ./generate-docs.sh

set -e

echo "🚀 Boardly API 문서 생성을 시작합니다..."

# 필요한 도구 설치 확인
check_dependency() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ $1이 설치되지 않았습니다. 설치해주세요."
        exit 1
    fi
}

echo "📋 의존성 확인 중..."
check_dependency "npx"

# OpenAPI 스펙 검증
echo "🔍 OpenAPI 스펙 검증 중..."
if npx swagger-cli validate openapi.yaml; then
    echo "✅ OpenAPI 스펙 검증 완료"
else
    echo "❌ OpenAPI 스펙 검증 실패"
    exit 1
fi

# HTML 문서 생성 (ReDoc)
echo "📄 HTML 문서 생성 중..."
if npx redoc-cli bundle openapi.yaml -o index.html --title "Boardly API Documentation"; then
    echo "✅ HTML 문서 생성 완료: docs/api/index.html"
else
    echo "❌ HTML 문서 생성 실패"
    exit 1
fi

# JSON 형태로 변환
echo "📄 JSON 문서 생성 중..."
if npx swagger-cli bundle openapi.yaml -o openapi.json; then
    echo "✅ JSON 문서 생성 완료: docs/api/openapi.json"
else
    echo "❌ JSON 문서 생성 실패"
    exit 1
fi

# Postman 컬렉션 생성
echo "📦 Postman 컬렉션 생성 중..."
if npx openapi2postmanv2 -s openapi.yaml -o postman-collection.json; then
    echo "✅ Postman 컬렉션 생성 완료: docs/api/postman-collection.json"
else
    echo "⚠️  Postman 컬렉션 생성 실패 (선택사항)"
fi

# TypeScript 타입 정의 생성
echo "🔧 TypeScript 타입 정의 생성 중..."
if npx openapi-typescript openapi.yaml -o types.ts; then
    echo "✅ TypeScript 타입 정의 생성 완료: docs/api/types.ts"
else
    echo "⚠️  TypeScript 타입 정의 생성 실패 (선택사항)"
fi

echo ""
echo "🎉 API 문서 생성이 완료되었습니다!"
echo ""
echo "📁 생성된 파일들:"
echo "  - docs/api/index.html (HTML 문서)"
echo "  - docs/api/openapi.json (JSON 스펙)"
echo "  - docs/api/postman-collection.json (Postman 컬렉션)"
echo "  - docs/api/types.ts (TypeScript 타입)"
echo ""
echo "🌐 HTML 문서를 브라우저에서 열어보세요:"
echo "  open docs/api/index.html"
echo ""
echo "📚 다음 단계:"
echo "  1. 백엔드에서 이 스펙을 기반으로 API 구현"
echo "  2. 프론트엔드에서 클라이언트 코드 생성"
echo "  3. API 테스트 및 검증"
