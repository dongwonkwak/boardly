## Boardly Schema

Boardly의 데이터베이스 스키마와 마이그레이션(SQL) 스크립트를 관리하는 모듈입니다. Flyway를 사용하며 H2(PostgreSQL 호환 모드)와 PostgreSQL 두 환경을 모두 지원합니다.

### 디렉터리 구조
- `src/main/resources/db/migration/common/`: 공통 스크립트 (예: 초기 V1)
- `src/main/resources/db/migration/h2/`: H2 전용 스크립트 및 시드(`R__*`)
- `src/main/resources/db/migration/postgresql/`: PostgreSQL 전용 스크립트 및 시드(`R__*`)

현재 주요 버전 스크립트:
- `postgresql/V2__workspace_schema.sql`: 워크스페이스 기반 멀티 테넌트 스키마
- `h2/V2__workspace_schema.sql`: H2(PostgreSQL 모드) 대응 스키마
- 시드 데이터:
  - `h2/R__insert_dev_dummy_data.sql`
  - `postgresql/R__insert_local_dummy_data.sql`

### 스키마 개요 (요약)
- 핵심 엔티티: `users`, `workspaces`, `boards`, `lists`, `cards`, `labels`, `comments`, `activities`
- 권한/멤버십: `workspace_members`, `board_members`
- 초대: `invitations` (워크스페이스/보드 통합)
- 인덱스 최적화 및 PostgreSQL 무결성 트리거 포함
- 프로젝트 전반에서 문자열 기반 ID(접두어 포함)를 사용하므로, DB 컬럼은 `VARCHAR(50)`을 사용합니다(PostgreSQL/H2 공통). 

### 로컬 테스트 (Gradle Tasks)
아래 명령은 프로젝트 루트 기준에서 실행합니다.

- H2(PostgreSQL 호환 모드):
```bash
cd backend
./gradlew :boardly-schema:schemaValidateH2
```
설정: 파일 기반 H2(DB_CLOSE_DELAY=-1, MODE=PostgreSQL). 순서: Reset files → Clean → Migrate → Validate.

- PostgreSQL:
1) Docker로 로컬 DB 기동
```bash
docker compose up -d postgres
```
2) 검증 태스크 실행 (기본 예시: 컨테이너의 기본 `postgres` DB 사용)
```bash
cd backend
./gradlew :boardly-schema:schemaValidatePostgresql \
  -PpgUrl=jdbc:postgresql://localhost:5432/postgres \
  -PpgUser=postgres -PpgPassword=postgres \
  -PpgSchema=schema_validation
```
- 필요 시 docker-compose의 DB명에 맞춰 URL을 조정하세요. 예: `jdbc:postgresql://localhost:5432/boardly-db`
- 스키마는 태스크 실행 시 `schema_validation`으로 생성/정리됩니다.

### 애플리케이션(Flyway) 적용 경로 참고
`backend/boardly-app/src/main/resources/application-local.yml` 에서 Flyway 경로는 다음과 같이 설정되어 있습니다.
- `classpath:db/migration/common`
- `classpath:db/migration/postgresql`

### 마이그레이션 작성 규칙
- 스키마 변경: `V{number}__{description}.sql` (버전 스크립트)
- 반복 실행/시드: `R__{description}.sql` (멱등성 고려)
- 공통 스크립트는 `common/`, 데이터베이스 특화 스크립트는 해당 폴더에 배치
- 이미 적용된 버전 스크립트는 수정하지 말고 새로운 `V__`를 추가

### 트러블슈팅
- PostgreSQL 인증 오류: Gradle 태스크 실행 시 `-PpgUser/-PpgPassword/-PpgUrl` 값을 docker-compose 설정과 일치시키세요.
- 데이터베이스 없음: 컨테이너가 기동 중인지 확인하고, `-PpgUrl`의 DB명이 실제 존재하는지 확인하세요.
- H2 시드 실패: H2 전용 스키마는 기본값/타입 차이를 반영했습니다. 그래도 실패 시 `build/h2` 디렉터리를 삭제 후 재시도하세요.

### 참고
- 설계 배경 및 필드 정의는 `docs/db/schema.md` 를 참고하세요.
