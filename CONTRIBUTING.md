# Contributing to AccessIQ

Thank you for your interest in contributing to AccessIQ! This document provides guidelines and instructions for contributing.

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check the issue list as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

- Use a clear and descriptive title
- Describe the exact steps to reproduce the problem
- Provide specific examples to demonstrate the steps
- Describe the behavior you observed and what you expected
- Include screenshots and animated GIFs if possible
- Include the version of the application and your environment

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- Use a clear and descriptive title
- Provide a step-by-step description of the suggested enhancement
- Provide specific examples to demonstrate the steps
- Describe the current behavior and explain the desired behavior
- Explain why this enhancement would be useful

### Pull Requests

- Fill in the required template
- Do not include issue numbers in the PR title
- Include screenshots and animated GIFs in your pull request whenever possible
- Follow the style guide
- Document new code based on the Documentation Style Guide
- End all files with a newline

## Development Setup

### Prerequisites

- Java 17 JDK
- Maven 3.9+
- Docker & Docker Compose (optional)
- PostgreSQL 12+ or MySQL 8+ (for production)

### Building Locally

```bash
# Clone the repository
git clone https://github.com/AccessIQ/accessiq.git
cd accessiq

# Build the project
./mvnw clean install

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw verify

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run with debug logging
./mvnw test -Dlogging.level.com.accessiq=DEBUG
```

## Code Style Guidelines

### Java Code Style

- Follow Spring Boot conventions
- Use constructor-based dependency injection
- Use Lombok for boilerplate reduction
- Use SLF4J for logging
- Follow naming conventions:
  - Classes: PascalCase
  - Methods: camelCase
  - Variables: camelCase
  - Constants: UPPER_SNAKE_CASE

### Git Commit Messages

- Use the present tense (e.g., "Add feature" not "Added feature")
- Use the imperative mood (e.g., "Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference issues and pull requests liberally after the first line

### Pull Request Process

1. Ensure any install or build dependencies are documented
2. Update the README.md with details of changes to the interface
3. Update the CHANGELOG.md if applicable
4. Add yourself to the authors list if this is your first contribution

## Project Structure

```
src/
├── main/
│   ├── java/com/accessiq/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST Controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Exception handling
│   │   ├── mapper/           # DTO mapping
│   │   ├── model/            # JPA Entities
│   │   ├── repository/       # JPA Repositories
│   │   ├── security/         # JWT & Security
│   │   └── service/          # Business logic
│   └── resources/
│       ├── application.yml
│       └── ...
└── test/
    └── java/com/accessiq/
        ├── controller/       # Controller tests
        ├── repository/       # Repository tests
        ├── security/         # Security tests
        └── service/          # Service tests
```

## Testing Requirements

- Unit tests must achieve 80%+ coverage for new code
- Integration tests for API endpoints
- Security tests for authentication/authorization
- Use MockMvc for controller tests
- Use @DataJpaTest for repository tests

## Questions?

- Open an issue with the "question" label
- Join our discussions