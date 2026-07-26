# AccessIQ Development Guidelines

## Project Structure

```
AccessIQ/
├── src/
│   ├── main/
│   │   ├── java/com/accessiq/
│   │   │   ├── AccessiqApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── mapper/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/accessiq/
├── .github/workflows/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## Coding Standards

### Java
- Use Java 17 features
- Follow Spring Boot conventions
- Use constructor-based dependency injection
- Use Lombok for boilerplate reduction (where configured)
- Use SLF4J for logging

### Naming Conventions
- Classes: PascalCase
- Methods: camelCase
- Variables: camelCase
- Constants: UPPER_SNAKE_CASE
- Packages: lowercase

### Documentation
- Use Javadoc for public APIs
- Document all endpoints with OpenAPI annotations
- Keep README updated

## Build Commands

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build Docker image
docker build -t accessiq:latest .

# Run with Docker Compose
docker-compose up -d
```

## Testing

- Unit tests: 90%+ coverage
- Integration tests: Use Testcontainers
- Test profiles: Use application-test.yml
- Mock beans: Use @MockBean for dependencies

## Security

- Never commit secrets to version control
- Use environment variables for sensitive data
- Always validate input
- Use parameterized queries to prevent SQL injection
- Implement proper error handling

## Deployment

1. Build Docker image
2. Push to container registry
3. Deploy to Kubernetes or cloud platform
4. Run database migrations
5. Monitor health endpoints