# Developer Guide

## Getting Started

### Prerequisites

- Java 17 JDK
- Maven 3.9+
- Docker & Docker Compose (optional)
- IDE (IntelliJ IDEA, VS Code, Eclipse)

### IDE Setup

#### IntelliJ IDEA

1. Open project as Maven project
2. Import Maven projects
3. Set JDK 17 as project SDK
4. Enable annotation processing
5. Install Lombok plugin

#### VS Code

1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Install Lombok Annotations
4. Open project in workspace

### Project Setup

```bash
# Clone repository
git clone https://github.com/AccessIQ/accessiq.git
cd accessiq

# Import into IDE
# Run Maven to download dependencies
./mvnw clean install
```

## Development Workflow

### Branch Strategy

```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes
git add .
git commit -m "Add your feature"

# Push and create PR
git push origin feature/your-feature-name
```

### Code Standards

#### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Classes | PascalCase | `UserService` |
| Methods | camelCase | `createUser()` |
| Variables | camelCase | `userList` |
| Constants | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| Packages | lowercase | `com.accessiq.service` |

#### Code Style

- Use constructor injection (not field injection)
- Use final for immutable fields
- Keep methods short (< 50 lines)
- Use meaningful variable names
- Add Javadoc for public APIs

## Running the Application

### Development Mode

```bash
# With H2 (in-memory database)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# With MySQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### With Docker

```bash
docker-compose up -d
```

## Testing

### Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=UserServiceTest

# With debug logging
./mvnw test -Dlogging.level.com.accessiq=DEBUG
```

### Writing Tests

Follow the Arrange-Act-Assert pattern:

```java
@Test
void testCreateUser() {
    // Arrange
    when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
    
    // Act
    User user = userService.createUser("test@example.com", "password");
    
    // Assert
    assertThat(user.getEmail()).isEqualTo("test@example.com");
    verify(userRepository).save(any(User.class));
}
```

## Debugging

### Remote Debugging

```bash
# Add JVM args
MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
./mvnw spring-boot:run
```

### Log Debugging

```bash
# Enable debug logging
./mvnw spring-boot:run -Dlogging.level.com.accessiq=DEBUG

# Log to file
logging:
  file:
    name: logs/accessiq.log
```

## API Development

### Adding New Endpoints

1. Add DTO in `dto/` package
2. Add controller method in appropriate controller
3. Add service method in appropriate service
4. Add repository method if needed
5. Add tests

### Example: Adding a New API

```java
// DTO
public class CreateUserRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    // getters/setters
}

// Controller
@PostMapping("/users")
public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    User user = userService.createUser(request.getEmail(), request.getPassword());
    return ResponseEntity.ok(ApiResponse.success(toUserResponse(user)));
}
```

## Database Migrations

### Adding New Entities

1. Create entity class in `model/` package
2. Create repository in `repository/` package
3. Update `DataInitializer` if needed
4. Run application to create table

### Entity Guidelines

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    // Use proper column definitions
    // Add indexes for frequently queried columns
    // Use @Version for optimistic locking if needed
}
```

## Security Development

### Adding New Roles

1. Add to `RoleName` enum
2. Update security configuration
3. Add tests

### Adding New Permissions

1. Add `@PreAuthorize` annotation
2. Test with different roles
3. Document in API docs

## Build and Deployment

### Building

```bash
# Clean build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# Build Docker image
docker build -t accessiq:latest .
```

### CI/CD

Push to main branch to trigger deployment:

```bash
git add .
git commit -m "Your changes"
git push origin main
```

## Common Tasks

### Adding a New Service

1. Create service class in `service/` package
2. Add `@Service` annotation
3. Inject repositories
4. Add business logic
5. Create tests

### Adding a New Repository

1. Create interface in `repository/` package
2. Extend `JpaRepository`
3. Add custom queries if needed
4. Create tests

### Adding a New Controller

1. Create controller class in `controller/` package
2. Add `@RestController` annotation
3. Add `@RequestMapping`
4. Inject services
5. Add endpoints
6. Create tests

## Useful Commands

```bash
# Clean project
./mvnw clean

# Compile only
./mvnw compile

# Run checks
./mvnw verify

# Generate coverage report
./mvnw jacoco:report

# Open Swagger UI
open http://localhost:8080/swagger-ui.html

# View health
open http://localhost:8080/actuator/health
```

## Getting Help

- Check the [README](README.md)
- Read [Architecture.md](Architecture.md)
- Consult [API.md](API.md)
- Open an issue on GitHub