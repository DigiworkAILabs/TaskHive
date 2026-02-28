# Step 14 — NFR-MAIN-01: Unit + Integration Tests

**NFR IDs:** NFR-MAIN-01
**Effort:** ~1–2 weeks | **Dependencies:** None
**Commit:** Multiple commits — one per module

---

## SRS Requirement

> Unit + integration test coverage > 80% on service layer.

## Current State

Only 4 test files exist:
- `TaskhiveBackendApplicationTests.java` (smoke test)
- `FullFlowIntegrationTest.java` (integration)
- `AnalyticsServiceTest.java` (unit)
- `AuditServiceTest.java` (unit)

## Implementation Plan

### Phase 1: Test Infrastructure Setup

#### Modify: `pom.xml`

Add test dependencies:

```xml
<!-- Test containers for integration tests -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>

<!-- JaCoCo for coverage -->
<!-- (already added in Step 11) -->
```

#### New File: `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:tc:postgresql:16-alpine:///taskhive_test
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

### Phase 2: Auth Module Tests (most critical)

Commit: `test(auth): add auth service + controller tests`

| File | Type | What to Test |
|------|------|-------------|
| `AuthServiceTest.java` | Unit | Login, register, account lock, failed attempts |
| `TokenServiceTest.java` | Unit | Create/validate/revoke refresh tokens, hash verification |
| `PasswordServiceTest.java` | Unit | Password encoding, history check, validation rules |
| `AccountActivationServiceTest.java` | Unit | Token generation, activation flow, expiry |
| `PasswordResetServiceTest.java` | Unit | Reset request, token validation, password change |
| `JwtTokenProviderTest.java` | Unit | JWT creation, parsing, expiry detection |
| `JwtAuthenticationFilterTest.java` | Unit | Cookie extraction, filter chain, invalid token handling |

### Phase 3: Employee Module Tests

Commit: `test(employee): add employee service + controller tests`

| File | Type | What to Test |
|------|------|-------------|
| `EmployeeServiceTest.java` | Unit | CRUD, status transitions, soft delete, pagination |
| `EmployeeControllerTest.java` | Unit | Request validation, response mapping, auth |
| `ProfilePhotoServiceTest.java` | Unit | Upload, validate, delete photo |

### Phase 4: Task Module Tests

Commit: `test(task): add task service + controller tests`

| File | Type | What to Test |
|------|------|-------------|
| `TaskServiceTest.java` | Unit | CRUD, status transitions, assignment, search |
| `TaskControllerTest.java` | Unit | Endpoints, validation, pagination |
| `TaskAttachmentServiceTest.java` | Unit | Upload, validate type/size, access control |
| `TaskCommentServiceTest.java` | Unit | Add/list comments, access control |

### Phase 5: Notification + Analytics Tests

Commit: `test(notification): add notification service tests`

| File | Type | What to Test |
|------|------|-------------|
| `NotificationServiceTest.java` | Unit | Create, mark read, pagination |
| `EmailQueueServiceTest.java` | Unit | Queue, retry logic, status transitions |
| `WebSocketNotificationServiceTest.java` | Unit | Message sending, topic routing |

### Phase 6: Integration Tests

Commit: `test(integration): add end-to-end integration tests`

| File | What to Test |
|------|-------------|
| `AuthIntegrationTest.java` | Full login → JWT → protected endpoint flow |
| `EmployeeIntegrationTest.java` | Full employee CRUD with DB |
| `TaskIntegrationTest.java` | Full task lifecycle with assignments |

### Testing Patterns

**Unit Test Template:**
```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {

    @Mock private SomeRepository repository;
    @InjectMocks private SomeService service;

    @Test
    void shouldDoSomething_whenCondition() {
        // Arrange
        when(repository.findById(any())).thenReturn(Optional.of(entity));

        // Act
        var result = service.doSomething(id);

        // Assert
        assertThat(result).isNotNull();
        verify(repository).findById(id);
    }
}
```

**Integration Test Template:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class SomeIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteFullFlow() { ... }
}
```

## Coverage Target

```bash
# Run tests with coverage
mvn clean verify

# View JaCoCo report
open target/site/jacoco/index.html

# Target: ≥ 80% line coverage on service layer
```

## Verification

```bash
mvn clean test
# All tests should pass

mvn jacoco:report
# Coverage report at target/site/jacoco/index.html
# Service layer coverage should be ≥ 80%
```
