# 의존성 관리 가이드

## 개요

Boardly 프로젝트는 Gradle의 Version Catalog를 사용하여 의존성을 중앙 집중식으로 관리합니다. 이 문서는 `libs.versions.toml` 파일을 통한 버전 관리와 각 모듈별 의존성 설정 방법을 설명합니다.

## Version Catalog 구조

### 1. libs.versions.toml 파일 구조

```toml
[versions]
# 버전 정의
java = "21"
spring-boot = "3.4.1"
postgres = "42.7.8"

[libraries]
# 라이브러리 정의
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
postgres = { module = "org.postgresql:postgresql", version.ref = "postgres" }

[plugins]
# 플러그인 정의
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }

[bundles]
# 번들 정의
spring-boot-starter = [
    "spring-boot-starter",
    "spring-boot-starter-web",
    "spring-boot-starter-data-jpa",
    "spring-boot-starter-validation",
]
```

### 2. 현재 사용 중인 버전들

```toml
[versions]
# Java
java = "21"

# Spring Boot
spring-boot = "3.4.1"
spring-dependency-management = "1.1.7"

# Database
h2 = "2.3.232"
postgres = "42.7.8"

# Lombok
lombok = "1.18.38"
lombok-mapstruct-binding = "0.2.0"

# JPA & QueryDSL
querydsl = "5.0.0:jakarta"

# Test
junit-jupiter = "5.11.4"
mockito = "5.14.2"
testcontainers = "1.20.4"
archunit = "1.4.1"

# Logging
logback = "1.5.12"

# Validation
validation = "3.1.0"

# Security
spring-security = "6.4.2"

# Documentation
springdoc-openapi = "2.8.9"

# Database Migration
flyway = "11.1.1"

# MapStruct
mapstruct = "1.6.3"

# Vavr
vavr = "0.10.7"

# ULID
ulid-creator = "5.2.3"
```

## 모듈별 의존성 설정

### 1. boardly-domain

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
    // Lombok
    compileOnly libs.lombok
    annotationProcessor libs.lombok
    
    // Validation
    implementation libs.validation.api
    
    // Vavr (Either, Option 등)
    implementation libs.vavr
    
    // ULID
    implementation libs.ulid.creator
    
    // Test
    testImplementation libs.junit.jupiter
    testImplementation libs.mockito.junit.jupiter
}
```

### 2. boardly-application

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation project(':boardly-domain')
    
    // Spring Boot
    implementation libs.spring.boot.starter
    implementation libs.spring.boot.starter.validation
    
    // MapStruct
    implementation libs.mapstruct
    annotationProcessor libs.mapstruct.processor
    
    // Lombok
    compileOnly libs.lombok
    annotationProcessor libs.lombok
    
    // Test
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.junit.jupiter
    testImplementation libs.mockito.junit.jupiter
}
```

### 3. boardly-infrastructure

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation project(':boardly-domain')
    implementation project(':boardly-application')
    
    // Spring Boot
    implementation libs.spring.boot.starter.data.jpa
    
    // Database
    implementation libs.postgres
    runtimeOnly libs.h2
    
    // QueryDSL
    implementation libs.querydsl.jpa
    annotationProcessor libs.querydsl.apt
    
    // Database Migration
    implementation libs.flyway.core
    implementation libs.flyway.database.postgresql
    
    // MapStruct
    implementation libs.mapstruct
    annotationProcessor libs.mapstruct.processor
    
    // Lombok
    compileOnly libs.lombok
    annotationProcessor libs.lombok
    
    // Test
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.testcontainers.junit.jupiter
    testImplementation libs.testcontainers.postgresql
}
```

### 4. boardly-api

```gradle
plugins {
    id 'java-library'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation project(':boardly-application')
    implementation project(':boardly-domain')
    
    // Spring Boot Web
    implementation libs.spring.boot.starter.web
    
    // Documentation
    implementation libs.springdoc.openapi.starter.webmvc.ui
    
    // Lombok
    compileOnly libs.lombok
    annotationProcessor libs.lombok
    
    // Test
    testImplementation libs.spring.boot.starter.test
    testImplementation libs.mockito.junit.jupiter
}
```

### 5. boardly-app

```gradle
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(libs.versions.java.get()) }
}

repositories { mavenCentral() }

dependencies {
    implementation project(':boardly-api')
    implementation project(':boardly-infrastructure')
    
    // Spring Boot
    implementation libs.spring.boot.starter
    implementation libs.spring.boot.starter.actuator
    
    // Monitoring
    implementation libs.micrometer.registry.prometheus
    
    // Test
    testImplementation libs.spring.boot.starter.test
}
```

## 번들(Bundle) 사용

### 1. Spring Boot Starter 번들

```toml
[bundles]
spring-boot-starter = [
    "spring-boot-starter",
    "spring-boot-starter-web",
    "spring-boot-starter-data-jpa",
    "spring-boot-starter-validation",
]
spring-boot-test = ["spring-boot-starter-test", "spring-security-test"]
testcontainers = ["testcontainers-junit-jupiter", "testcontainers-postgresql"]
archunit-test = ["archunit", "archunit-junit5"]
```

### 2. 번들 사용 예시

```gradle
dependencies {
    // 번들 사용
    implementation libs.bundles.spring.boot.starter
    testImplementation libs.bundles.spring.boot.test
    testImplementation libs.bundles.testcontainers
}
```

## 플러그인 관리

### 1. 플러그인 정의

```toml
[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "spring-boot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "spring-dependency-management" }
querydsl = { id = "com.ewerk.gradle.plugins.querydsl", version = "1.0.10" }
```

### 2. 플러그인 사용

```gradle
plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.querydsl)
}
```

## 버전 업데이트 전략

### 1. 정기 업데이트

```bash
# 의존성 업데이트 확인
./gradlew dependencyUpdates

# 특정 의존성 업데이트
./gradlew dependencyUpdates --group=org.springframework.boot
```

### 2. 버전 업데이트 절차

1. **개발 환경에서 테스트**
   ```bash
   # 버전 업데이트 후 빌드 테스트
   ./gradlew clean build
   
   # 테스트 실행
   ./gradlew test
   ```

2. **통합 테스트**
   ```bash
   # 통합 테스트 실행
   ./gradlew integrationTest
   ```

3. **문서 업데이트**
   - CHANGELOG.md 업데이트
   - 마이그레이션 가이드 작성 (필요시)

### 3. 보안 업데이트

```bash
# 보안 취약점 확인
./gradlew dependencyCheckAnalyze

# 보안 업데이트 적용
./gradlew dependencyCheckUpdate
```

## 의존성 분석

### 1. 의존성 트리 확인

```bash
# 전체 의존성 트리
./gradlew dependencies

# 특정 모듈의 의존성 트리
./gradlew :boardly-api:dependencies

# 특정 설정의 의존성 트리
./gradlew :boardly-api:dependencies --configuration compileClasspath
```

### 2. 중복 의존성 확인

```bash
# 중복 의존성 확인
./gradlew dependencyInsight --dependency=spring-core
```

### 3. 의존성 충돌 해결

```gradle
dependencies {
    // 특정 버전 강제 지정
    constraints {
        implementation('org.springframework:spring-core') {
            version {
                strictly '6.2.1'
            }
        }
    }
    
    // 의존성 제외
    implementation(libs.spring.boot.starter.web) {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
}
```

## 모범 사례

### 1. 버전 관리

```toml
# 좋은 예: 명확한 버전 관리
[versions]
spring-boot = "3.4.1"
postgres = "42.7.8"

# 나쁜 예: 동적 버전
[versions]
spring-boot = "3.+"
postgres = "latest"
```

### 2. 의존성 그룹화

```toml
# 관련 의존성을 그룹화
[libraries]
# Spring Boot
spring-boot-starter = { module = "org.springframework.boot:spring-boot-starter", version.ref = "spring-boot" }
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "spring-boot" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa", version.ref = "spring-boot" }

# Database
postgres = { module = "org.postgresql:postgresql", version.ref = "postgres" }
h2 = { module = "com.h2database:h2", version.ref = "h2" }

# Test
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit-jupiter" }
mockito-junit-jupiter = { module = "org.mockito:mockito-junit-jupiter", version.ref = "mockito" }
```

### 3. 번들 활용

```toml
# 자주 함께 사용되는 의존성을 번들로 정의
[bundles]
spring-boot-starter = [
    "spring-boot-starter",
    "spring-boot-starter-web",
    "spring-boot-starter-data-jpa",
    "spring-boot-starter-validation",
]
spring-boot-test = ["spring-boot-starter-test", "spring-security-test"]
```

### 4. 모듈별 의존성 최소화

```gradle
// 좋은 예: 필요한 의존성만 포함
dependencies {
    implementation project(':boardly-domain')
    implementation libs.spring.boot.starter.validation
}

// 나쁜 예: 불필요한 의존성 포함
dependencies {
    implementation project(':boardly-domain')
    implementation project(':boardly-infrastructure') // 불필요
    implementation libs.spring.boot.starter.web // 불필요
}
```

## 문제 해결

### 1. 의존성 충돌

```bash
# 충돌하는 의존성 확인
./gradlew dependencyInsight --dependency=spring-core

# 해결 방법: 버전 강제 지정
dependencies {
    constraints {
        implementation('org.springframework:spring-core') {
            version {
                strictly '6.2.1'
            }
        }
    }
}
```

### 2. 순환 의존성

```bash
# 순환 의존성 확인
./gradlew projects --all

# 해결 방법: 의존성 방향 재설계
// boardly-api → boardly-application → boardly-domain
// boardly-infrastructure → boardly-domain
```

### 3. 빌드 성능 최적화

```gradle
// 빌드 캐시 활성화
org.gradle.caching=true

// 병렬 빌드 활성화
org.gradle.parallel=true

// 구성 캐시 활성화
org.gradle.configuration-cache=true
```

## 결론

Version Catalog를 사용한 중앙 집중식 의존성 관리는 버전 일관성과 유지보수성을 크게 향상시킵니다. 이 문서의 가이드를 따라 체계적인 의존성 관리를 통해 안정적인 프로젝트를 유지하세요.
