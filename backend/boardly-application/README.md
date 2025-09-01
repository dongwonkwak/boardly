# Boardly Application Layer

## 개요

Boardly 애플리케이션 레이어는 도메인 로직을 조율하고 비즈니스 유스케이스를 구현하는 계층입니다.

## 구현된 기능

### 회원가입 (SignUp)

#### 구조
- `SignUpCommand`: 회원가입 요청 데이터를 담는 명령 객체
- `SignUpUseCase`: 회원가입 비즈니스 로직을 처리하는 유스케이스

#### 주요 기능
1. **입력 검증**
   - 이메일 형식 검증
   - 사용자명 형식 및 길이 검증 (3-20자, 영문/숫자/언더스코어/하이픈만 허용)
   - 비밀번호 길이 검증 (8-100자)
   - 표시명 길이 검증 (최대 50자)

2. **중복 검사**
   - 이메일 중복 확인
   - 사용자명 중복 확인

3. **비밀번호 암호화**
   - 입력된 비밀번호를 암호화하여 저장

4. **사용자 생성**
   - 도메인 모델을 사용하여 사용자 엔티티 생성
   - 기본 상태를 ACTIVE로 설정

#### 에러 처리
- `Failure.InputError`: 입력 검증 실패 시 (400 Bad Request)
- `Failure.ResourceConflict`: 중복 데이터 존재 시 (409 Conflict)
- `Failure.InternalError`: 내부 서버 오류 시 (500 Internal Server Error)

#### 테스트
- `SignUpCommandTest`: 명령 객체 생성 테스트
- `SignUpUseCaseTest`: 유스케이스 비즈니스 로직 테스트
  - 성공 케이스
  - 이메일/사용자명 중복 실패 케이스
  - 입력 검증 실패 케이스들

## 아키텍처 원칙

1. **Hexagonal Architecture**: 도메인 중심의 의존성 역전
2. **Command Pattern**: 명령 객체를 통한 요청 캡슐화
3. **Either Pattern**: 함수형 에러 처리
4. **Domain-Driven Design**: 도메인 모델 중심 설계

## 사용 예시

```java
// 회원가입 명령 생성
SignUpCommand command = SignUpCommand.builder()
    .email("user@example.com")
    .username("username")
    .password("password123")
    .displayName("사용자")
    .build();

// 유스케이스 실행
Either<Failure, User> result = signUpUseCase.execute(command);

if (result.isRight()) {
    User user = result.get();
    // 성공 처리
} else {
    Failure failure = result.getLeft();
    // 실패 처리
}
```
