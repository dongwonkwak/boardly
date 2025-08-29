# Development Workflow — Boardly (with Linear × GitHub × Cursor bugbot)

Boardly 프로젝트의 표준 개발 워크플로우를 정의한 문서입니다. Linear 이슈 관리, GitHub 브랜치/PR, Cursor bugbot 리뷰를 통합해 end-to-end 추적성을 확보합니다.

---

## 1. 이슈 기반 개발 시작

* Linear에서 사이클 → Epic → Issue 확인
* Issue Tracker 문서에서 상태를 **Todo**로 변경

브랜치 네이밍 규칙:

```bash
git checkout -b feat/{domain}-{feature}
# 예: feat/auth-login
```

커밋 메시지 규칙:

```bash
git commit -m "[BRD-102] 로그인 API 및 UI 구현"
```

---

## 2. 개발 & 커밋

* Epic & Issues / Task Tracker 문서를 참조해 개발
* 단위 테스트/통합 테스트 작성 및 실행
* 커밋 시 Linear 이슈 키 반드시 포함

---

## 3. PR 생성

* GitHub에서 Pull Request 생성

  * base: `main` (또는 `develop`)
  * compare: `feat/{domain}-{feature}`
* 제목 규칙: `[BRD-XXX] 기능명`

  * 예: `[BRD-102] 로그인 기능 구현`
* 본문: 구현 내용, 테스트 방법, 관련 문서(Task Tracker 등) 링크

---

## 4. bugbot 자동 리뷰

* PR 생성/업데이트 시 **Cursor bugbot이 자동 리뷰 수행**

  * 코드 변경 분석
  * 잠재적 버그/안전성 지적
  * 테스트 케이스 제안
  * 리팩토링/성능 개선 조언
* 리뷰 결과는 **PR 대화 탭 코멘트**로 기록됨

---

## 5. 리뷰 반영 & 상태 업데이트

* bugbot 피드백 반영 후 추가 커밋
* Linear 상태 변경:

  * `In Progress` → `In Review`
* Issue Tracker 문서에 **PR 링크 기록**

예시:

```markdown
| Cycle | Epic | Issue | Linear Key | GitHub 브랜치/PR | 상태 |
|-------|------|-------|------------|------------------|------|
| Cycle 1 | 사용자 인증/인가 | 로그인 기능 구현 | BRD-102 | `feat/auth-login` → [PR #15](https://github.com/org/repo/pull/15) | In Review |
```

---

## 6. 머지 & 완료 처리

* bugbot 리뷰 + self-review 완료 후 머지
* Linear 상태 변경: `In Review` → `Done`
* Issue Tracker 문서 상태 업데이트 (Done)

---

## 📊 흐름 요약 (End-to-End 추적)

1. Linear Issue 생성 (Epic & Issues 기반)
2. 브랜치 생성 & 개발
3. PR 생성 → bugbot 자동 리뷰
4. PR 머지 → Linear 상태 Done 업데이트
5. Issue Tracker에 최종 기록

