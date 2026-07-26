# Testing Documentation

## Overview

AccessIQ uses a comprehensive testing strategy with JUnit 5, Mockito, and Spring Boot test utilities.

## Testing Framework

| Framework | Version | Purpose |
|-----------|---------|---------|
| JUnit 5 | 5.10+ | Unit & Integration Testing |
| Mockito | 5.4+ | Mocking Framework |
| AssertJ | 3.24+ | Fluent Assertions |
| Spring Boot Test | 3.2.5 | Integration Testing |
| Testcontainers | 1.19+ | Database Testing |

## Test Categories

### Unit Tests
- Test individual classes in isolation
- Use `@ExtendWith(MockitoExtension.class)`
- Mock all external dependencies
- Fast execution

### Integration Tests
- Test component interactions
- Use `@SpringBootTest`
- Real database connections
- Slower but more comprehensive

### Controller Tests
- Use `@WebMvcTest`
- Mock service layer
- Test HTTP endpoints
- Validate request/response

### Repository Tests
- Use `@DataJpaTest`
- In-memory database (H2)
- Test JPA mappings
- Test queries

## Test Structure

```
src/test/java/com/accessiq/
├── controller/
│   └── AuthControllerTest.java
├── repository/
│   └── UserRepositoryTest.java
├── security/
│   └── JwtTokenProviderTest.java
└── service/
    ├── RequestServiceTest.java
    └── UserServiceTest.java
```

## JUnit 5 Features Used

### Test Annotations

```java
@Test                    // Unit test method
@BeforeEach             // Setup before each test
@BeforeAll              // Setup once before all tests
@ParameterizedTest      // Parameterized test
@DisplayName            // Custom test name
@Tag                    // Test categorization
```

### Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

assertEquals(expected, actual);
assertNotNull(object);
assertThrows(Exception.class, () -> method());
assertTimeout(Duration.ofSeconds(5), () -> method());
```

## Mockito Usage

### Annotations

```java
@Mock                    // Create mock
@InjectMocks            // Inject mocks
@Spy                   // Create spy
@Captor                 // Capture arguments
```

### Verification

```java
verify(mock).method();
verify(mock, times(2)).method();
verifyNoInteractions(mock);
ArgumentCaptor<T> captor = ArgumentCaptor.forClass(T.class);
verify(mock).method(captor.capture());
```

## MockMvc Testing

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
```

## @DataJpaTest

```java
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        userRepository.save(user);
        
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
    }
}
```

## Test Coverage

### JaCoCo Configuration

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.15</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Running Tests

### Maven Commands

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw verify

# Run specific test
./mvnw test -Dtest=UserServiceTest

# Run skipped tests
./mvnw test -DskipTests=false

# Generate report
./mvnw jacoco:report
```

### Test Reports

Reports are generated at:
- `target/surefire-reports/` - Test results
- `target/jacoco/` - Coverage reports

## Testing Strategy

### Unit Test Strategy
- Test one class per test file
- Mock all dependencies
- Test edge cases and error conditions
- Keep tests fast (< 100ms each)

### Integration Test Strategy
- Test component interactions
- Use real database connections
- Test complete workflows
- Cover API endpoints

### Test Data Management
- Use @BeforeEach for setup
- Clean database between tests
- Use test-specific data
- Avoid hardcoded test data

## Mocking Strategy

### When to Mock
- External services (email, payment)
- Database repositories (for unit tests)
- Third-party APIs

### When Not to Mock
- The class under test
- Internal dependencies being tested
- Value objects

## Best Practices

1. **One assertion per test** - Or use assertion groups
2. **Descriptive test names** - Use @DisplayName
3. **Arrange-Act-Assert pattern** - Clear test structure
4. **Test edge cases** - Null, empty, invalid inputs
5. **Clean up resources** - Use @AfterEach
6. **Parallel execution** - Configure surefire for parallel tests
7. **Test coverage** - Aim for 80%+ on business logic

## Continuous Integration

GitHub Actions runs tests on:
- Every push to main branch
- Every pull request
- With coverage reporting

See [CI-CD.md](CI-CD.md) for details.