# Getting Started(로컬 개발 가이드)

로컬에서 Boardly를 실행하기 위한 최소 절차입니다.

## 사전 요구사항
- Java 21+
- Gradle(래퍼 포함)
- Node.js 18+ 및 패키지 매니저(npm 또는 yarn/pnpm)
- Docker(선택) — 로컬 DB 실행 시 유용

## 환경 변수/설정
- 백엔드 프로파일: `local`, `dev`
  - `backend/boardly-app/src/main/resources/application-local.yml`
  - 필요시 `application-dev.yml`
- i18n 메시지: `backend/boardly-api/src/main/resources/messages/*.properties`

## 데이터베이스 준비
### Docker(PostgreSQL) 예시
```bash
docker run --name boardly-pg -e POSTGRES_PASSWORD=boardly -e POSTGRES_DB=boardly -p 5432:5432 -d postgres:16
```
- 애플리케이션의 DB 접속 정보는 `application-local.yml`에 맞춰 수정

### Flyway 마이그레이션
- 애플리케이션 기동 시 자동 적용됩니다.
- 공통 스키마: `backend/boardly-infrastructure/src/main/resources/db/migration/common`
- 시드 데이터: `db/migration/{local,dev}/R__insert_*_dummy_data.sql`

## 백엔드 실행
```bash
./gradlew :backend:boardly-app:bootRun --args='--spring.profiles.active=local'
```
- 기본 포트: 8080(가정)

## 프론트엔드 실행
```bash
cd frontend
npm install # 또는 yarn / pnpm
npm run dev
```
- 기본 포트: 3000(가정), 프록시 설정은 `frontend/src/services/api/client.ts` 참고

## 유용한 스크립트(예시)
- 테스트: `./gradlew test`
- 정적 분석/포맷(있을 경우): `./gradlew check`

## 트러블슈팅
- DB 연결 오류: 컨테이너/포트, `application-local.yml`의 접속 정보 확인
- 마이그레이션 실패: 이미 적용된 스크립트는 수정하지 말고 새 `V__` 추가
- CORS 문제: `SecurityConfig`의 CORS 설정 확인
