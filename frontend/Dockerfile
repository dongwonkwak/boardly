# 멀티 스테이지 빌드를 위한 베이스 이미지
FROM node:20-alpine AS base

# 작업 디렉토리 설정
WORKDIR /app

# 패키지 파일 복사
COPY package.json pnpm-lock.yaml ./

# pnpm 설치
RUN npm install -g pnpm

# 의존성 설치
RUN pnpm install --frozen-lockfile

# 개발 스테이지
FROM base AS development

# 소스 코드 복사
COPY . .

# 개발 서버 포트 노출
EXPOSE 5173

# 개발 서버 실행
CMD ["pnpm", "dev", "--host", "0.0.0.0"]

# 빌드 스테이지
FROM base AS builder

# 소스 코드 복사
COPY . .

# 프로덕션 빌드
RUN pnpm run build

# 프로덕션 스테이지
FROM nginx:alpine AS production

# nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 빌드된 파일들을 nginx 서버로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 포트 노출
EXPOSE 80

# nginx 시작
CMD ["nginx", "-g", "daemon off;"] 