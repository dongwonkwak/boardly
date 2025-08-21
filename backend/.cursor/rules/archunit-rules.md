# ArchUnit 아키텍처 테스트 규칙

## 1. ArchUnit 기본 설정

### 1.1 의존성 추가
```gradle
// build.gradle
dependencies {
    testImplementation 'com.tngtech.archunit:archunit-junit5:1.1.0'
    testImplementation 'com.tngtech.archunit:archunit:1.1.0'
}
```

### 1.2 기본 테스트 클래스 구조
```java
package com.boardly.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Boardly 헥사고날 아키텍처 ArchUnit 테스트
 * 
 * 모듈 구조:
 * - boardly-domain: 도메인 계층 (엔티티, 도메인 서비스, 포트)
 * - boardly-application: 애플리케이션 계층 (유스케이스, 애플리케이션 서비스)
 * - boardly-infrastructure: 인프라 계층 (어댑터, 리포지토리 구현체)
 * - boardly-api: 프레젠테이션 계층 (컨트롤러, DTO)
 */
public class HexagonalArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.boardly");
    }
}
```

## 2. 계층 구조 검증 규칙

### 2.1 헥사고날 계층 구조 검증
```java
@Test
@DisplayName("헥사고날 계층 구조 검증")
void hexagonalLayerArchitectureShouldBeRespected() {
    ArchRule rule = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            
            // 계층 정의
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Presentation").definedBy("..api..")
            
            // 의존성 규칙
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Presentation")
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer();

    rule.check(importedClasses);
}
```

### 2.2 모듈 간 의존성 검증
```java
@Test
@DisplayName("모듈 간 의존성 검증")
void modulesShouldNotDependOnEachOtherDirectly() {
    // 도메인 모듈은 다른 모듈에 의존하지 않음
    ArchRule domainRule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..api..");

    // 애플리케이션 모듈은 인프라와 API에 의존하지 않음
    ArchRule applicationRule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..api..");

    // API 모듈은 인프라에 의존하지 않음
    ArchRule apiRule = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    domainRule.check(importedClasses);
    applicationRule.check(importedClasses);
    apiRule.check(importedClasses);
}
```

## 3. 도메인 계층 검증 규칙

### 3.1 도메인 계층은 외부 프레임워크에 의존하지 않음
```java
@Test
@DisplayName("도메인 계층은 외부 프레임워크에 의존하지 않아야 함")
void domainShouldNotDependOnFrameworks() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "javax.persistence..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "com.fasterxml.jackson..",
                    "org.springframework.data.."
            );

    rule.check(importedClasses);
}
```

### 3.2 도메인 엔티티는 프레임워크 어노테이션을 사용하지 않음
```java
@Test
@DisplayName("도메인 엔티티는 프레임워크 어노테이션을 사용하지 않아야 함")
void domainEntitiesShouldNotUseFrameworkAnnotations() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Entity")
            .or().haveSimpleNameEndingWith("Aggregate")
            .or().haveSimpleNameEndingWith("ValueObject")
            .should().beAnnotatedWith(Component.class)
            .orShould().beAnnotatedWith(Service.class)
            .orShould().beAnnotatedWith(Repository.class)
            .orShould().beAnnotatedWith(Entity.class)
            .orShould().beAnnotatedWith(Table.class);

    rule.check(importedClasses);
}
```

### 3.3 포트(인터페이스)는 도메인 패키지에 위치
```java
@Test
@DisplayName("포트(인터페이스)는 도메인 패키지에 위치해야 함")
void portsShouldBeInDomainPackage() {
    // 인바운드 포트 (UseCase)
    ArchRule inboundPortRule = classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .or().haveSimpleNameEndingWith("Port")
            .and().areInterfaces()
            .should().resideInAPackage("..domain.port..");

    // 아웃바운드 포트 (Repository)
    ArchRule outboundPortRule = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .and().areInterfaces()
            .should().resideInAPackage("..domain.port..");

    inboundPortRule.check(importedClasses);
    outboundPortRule.check(importedClasses);
}
```

### 3.4 도메인 서비스는 도메인 계층만 의존
```java
@Test
@DisplayName("도메인 서비스는 다른 도메인 서비스와 엔티티만 의존해야 함")
void domainServicesShouldOnlyDependOnDomainLayer() {
    ArchRule rule = classes()
            .that().resideInAPackage("..domain..service..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                    "..domain..",
                    "java..",
                    "javax.validation..",
                    "jakarta.validation.."
            );

    rule.check(importedClasses);
}
```

## 4. 애플리케이션 계층 검증 규칙

### 4.1 애플리케이션 서비스는 애플리케이션 계층에 위치
```java
@Test
@DisplayName("애플리케이션 서비스는 애플리케이션 계층에 위치해야 함")
void applicationServicesShouldBeInApplicationLayer() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .or().haveSimpleNameEndingWith("Service")
            .and().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..application..");

    rule.check(importedClasses);
}
```

### 4.2 UseCase 구현체는 Service 어노테이션이 있어야 함
```java
@Test
@DisplayName("UseCase 구현체는 Service 어노테이션이 있어야 함")
void useCaseImplShouldBeAnnotatedWithService() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .and().resideInAPackage("..application..")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(Service.class);

    rule.check(importedClasses);
}
```

### 4.3 애플리케이션 서비스는 포트를 통해서만 도메인에 접근
```java
@Test
@DisplayName("애플리케이션 서비스는 포트를 통해서만 도메인에 접근해야 함")
void applicationServicesShouldOnlyAccessDomainThroughPorts() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .and().areAnnotatedWith(Service.class)
            .should().dependOnClassesThat()
            .resideInAPackage("..domain..")
            .and().haveSimpleNameNotEndingWith("Port")
            .and().haveSimpleNameNotEndingWith("UseCase");

    rule.check(importedClasses);
}
```

## 5. 인프라 계층 검증 규칙

### 5.1 어댑터는 인프라 계층에 위치
```java
@Test
@DisplayName("어댑터는 인프라 계층에 위치해야 함")
void adaptersShouldBeInInfrastructureLayer() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Adapter")
            .or().haveSimpleNameEndingWith("RepositoryImpl")
            .or().haveSimpleNameEndingWith("JpaRepository")
            .should().resideInAPackage("..infrastructure..");

    rule.check(importedClasses);
}
```

### 5.2 Repository 구현체는 인터페이스를 구현
```java
@Test
@DisplayName("Repository 구현체는 인터페이스를 구현해야 함")
void repositoryImplShouldImplementInterface() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("RepositoryImpl")
            .or().haveSimpleNameEndingWith("JpaRepository")
            .should().implement(classes().that().areInterfaces());

    rule.check(importedClasses);
}
```

### 5.3 인프라 계층은 도메인을 참조할 수 있지만 역방향은 불가
```java
@Test
@DisplayName("인프라 계층은 도메인을 참조할 수 있지만 역방향은 불가")
void infrastructureCanDependOnDomainButNotViceVersa() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    rule.check(importedClasses);
}
```

### 5.4 Config 클래스는 infrastructure.config 패키지에 위치
```java
@Test
@DisplayName("Config 클래스는 infrastructure.config 패키지에 위치해야 함")
void configClassesShouldBeInConfigPackage() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Config")
            .or().haveSimpleNameEndingWith("Configuration")
            .should().resideInAPackage("..infrastructure.config..");

    rule.check(importedClasses);
}
```

## 6. 프레젠테이션 계층 검증 규칙

### 6.1 컨트롤러는 프레젠테이션 계층에 위치
```java
@Test
@DisplayName("컨트롤러는 프레젠테이션 계층에 위치해야 함")
void controllersShouldBeInPresentationLayer() {
    ArchRule rule = classes()
            .that().areAnnotatedWith(RestController.class)
            .or().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..api..");

    rule.check(importedClasses);
}
```

### 6.2 프레젠테이션 계층은 인프라에 의존하지 않음
```java
@Test
@DisplayName("프레젠테이션 계층은 인프라에 의존하지 않음")
void presentationLayerDependencyRule() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    rule.check(importedClasses);
}
```

### 6.3 DTO는 프레젠테이션 계층에만 위치
```java
@Test
@DisplayName("DTO는 프레젠테이션 계층에만 위치해야 함")
void dtosShouldBeInPresentationLayer() {
    ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Dto")
            .or().haveSimpleNameEndingWith("DTO")
            .or().haveSimpleNameEndingWith("Request")
            .or().haveSimpleNameEndingWith("Response")
            .should().resideInAPackage("..api.dto..");

    rule.check(importedClasses);
}
```

## 7. 예외 처리 검증 규칙

### 7.1 Exception 클래스들의 패키지 구조 검증
```java
@Test
@DisplayName("Exception 클래스들의 패키지 구조 검증")
void exceptionsShouldBeInCorrectPackage() {
    // 도메인 예외
    ArchRule domainExceptionRule = classes()
            .that().haveSimpleNameEndingWith("Exception")
            .and().resideInAPackage("..domain..")
            .should().resideInAPackage("..domain.exception..");

    // 애플리케이션 예외
    ArchRule applicationExceptionRule = classes()
            .that().haveSimpleNameEndingWith("Exception")
            .and().resideInAPackage("..application..")
            .should().resideInAPackage("..application.exception..");

    // API 예외
    ArchRule apiExceptionRule = classes()
            .that().haveSimpleNameEndingWith("Exception")
            .and().resideInAPackage("..api..")
            .should().resideInAPackage("..api.exception..");

    domainExceptionRule.check(importedClasses);
    applicationExceptionRule.check(importedClasses);
    apiExceptionRule.check(importedClasses);
}
```

## 8. ULID 사용 검증 규칙

### 8.1 도메인 ID는 prefix를 사용해야 함
```java
@Test
@DisplayName("도메인 ID는 prefix를 사용해야 함")
void domainIdsShouldUsePrefix() {
    ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Id")
            .should().haveSimpleNameMatching(".*Id")
            .andShould().haveOnlyPrivateConstructors()
            .andShould().haveMethod("generate")
            .andShould().haveMethod("of", String.class);

    rule.check(importedClasses);
}
```

### 8.2 ID 클래스는 Value 어노테이션을 사용해야 함
```java
@Test
@DisplayName("ID 클래스는 Value 어노테이션을 사용해야 함")
void idClassesShouldUseValueAnnotation() {
    ArchRule rule = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Id")
            .should().beAnnotatedWith(Value.class);

    rule.check(importedClasses);
}
```

## 9. 네이밍 컨벤션 검증 규칙

### 9.1 클래스 네이밍 규칙
```java
@Test
@DisplayName("클래스 네이밍 규칙 검증")
void classNamingConventions() {
    // 엔티티는 명사로 끝나야 함
    ArchRule entityRule = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Entity")
            .should().haveSimpleNameMatching("[A-Z][a-zA-Z0-9]*Entity");

    // UseCase는 UseCase로 끝나야 함
    ArchRule useCaseRule = classes()
            .that().resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("UseCase")
            .should().haveSimpleNameMatching("[A-Z][a-zA-Z0-9]*UseCase");

    // Repository는 Repository로 끝나야 함
    ArchRule repositoryRule = classes()
            .that().resideInAPackage("..domain.port..")
            .and().haveSimpleNameEndingWith("Repository")
            .should().haveSimpleNameMatching("[A-Z][a-zA-Z0-9]*Repository");

    entityRule.check(importedClasses);
    useCaseRule.check(importedClasses);
    repositoryRule.check(importedClasses);
}
```

### 9.2 패키지 네이밍 규칙
```java
@Test
@DisplayName("패키지 네이밍 규칙 검증")
void packageNamingConventions() {
    // 모든 패키지는 소문자로만 구성
    ArchRule rule = classes()
            .should().resideInAPackage("..")
            .because("패키지명은 소문자로만 구성되어야 함");

    rule.check(importedClasses);
}
```

## 10. 성능 및 보안 검증 규칙

### 10.1 트랜잭션 어노테이션 사용 검증
```java
@Test
@DisplayName("UseCase는 트랜잭션 어노테이션을 사용해야 함")
void useCaseShouldUseTransactionalAnnotation() {
    ArchRule rule = classes()
            .that().resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("UseCase")
            .and().areNotInterfaces()
            .should().beAnnotatedWith(Transactional.class);

    rule.check(importedClasses);
}
```

### 10.2 로깅 사용 검증
```java
@Test
@DisplayName("UseCase는 로깅을 사용해야 함")
void useCaseShouldUseLogging() {
    ArchRule rule = classes()
            .that().resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("UseCase")
            .and().areNotInterfaces()
            .should().haveField("log")
            .orShould().haveField("logger");

    rule.check(importedClasses);
}
```

## 11. 테스트 구조 검증 규칙

### 11.1 테스트 클래스 네이밍 규칙
```java
@Test
@DisplayName("테스트 클래스 네이밍 규칙 검증")
void testClassNamingConventions() {
    ArchRule rule = classes()
            .that().resideInAPackage("..test..")
            .or().resideInAPackage("..tests..")
            .should().haveSimpleNameEndingWith("Test")
            .orShould().haveSimpleNameEndingWith("Tests");

    rule.check(importedClasses);
}
```

### 11.2 테스트는 테스트 패키지에만 위치
```java
@Test
@DisplayName("테스트는 테스트 패키지에만 위치해야 함")
void testsShouldBeInTestPackage() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("..test..")
            .or().resideInAPackage("..tests..")
            .should().dependOnClassesThat()
            .resideInAPackage("..test..", "..tests..");

    rule.check(importedClasses);
}
```

## 12. AI 코딩 가이드라인

### ArchUnit 테스트 작성 시 준수사항
1. **모든 아키텍처 규칙은 ArchUnit 테스트로 검증**
2. **계층 간 의존성 규칙을 명확히 정의**
3. **패키지 구조와 네이밍 규칙을 검증**
4. **프레임워크 의존성을 적절한 계층에만 허용**
5. **도메인 계층의 순수성을 보장**
6. **포트와 어댑터 패턴을 검증**
7. **ULID 사용 규칙을 검증**
8. **예외 처리 구조를 검증**

### 금지사항
1. **도메인 계층에 프레임워크 의존성 포함 금지**
2. **계층 간 역방향 의존성 금지**
3. **잘못된 패키지에 클래스 배치 금지**
4. **인터페이스 없이 구현체 직접 사용 금지**
5. **DTO를 도메인 계층에 배치 금지**
6. **컨트롤러를 인프라 계층에 배치 금지**

## 13. 실행 방법

### 13.1 전체 테스트 실행
```bash
./gradlew test --tests "*ArchitectureTest"
```

### 13.2 특정 규칙만 실행
```bash
./gradlew test --tests "*ArchitectureTest.hexagonalLayerArchitectureShouldBeRespected"
```

### 13.3 CI/CD 파이프라인에서 실행
```yaml
# .github/workflows/test.yml
- name: Run Architecture Tests
  run: ./gradlew test --tests "*ArchitectureTest"
```

## 14. 문제 해결

### 14.1 의존성 위반 해결
- **순환 의존성**: 인터페이스를 통한 의존성 역전
- **잘못된 계층 의존성**: 적절한 계층으로 클래스 이동
- **프레임워크 의존성**: 어댑터 패턴을 통한 격리

### 14.2 성능 최적화
- **테스트 실행 시간**: ImportOption을 통한 불필요한 클래스 제외
- **메모리 사용량**: 클래스 필터링을 통한 최적화

### 14.3 유지보수
- **규칙 업데이트**: 새로운 아키텍처 요구사항 반영
- **예외 처리**: 불가피한 위반에 대한 예외 규칙 추가
