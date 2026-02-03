AccessIQ – Role-Based Workflow & Approval System (Backend)

AccessIQ is an enterprise-grade backend application built using Spring Boot that provides secure authentication, role-based access control, workflow management, and audit logging.
The project is designed to simulate real-world approval systems used in large organizations.

🚀 Features

🔐 JWT-based Authentication & Authorization

👥 Role-Based Access Control (RBAC) (Admin, User, Approver)

🔄 Configurable Multi-Step Workflows

📝 Request Creation & Approval System

📊 Audit Logging for All Actions

♻️ Refresh Token Mechanism

📄 RESTful APIs with Swagger/OpenAPI

🗄️ MySQL Database Integration

🧩 Layered Architecture (Controller → Service → Repository)

🛠️ Tech Stack
Layer	Technology
Language	Java 17
Framework	Spring Boot
Security	Spring Security + JWT
Database	MySQL 8
ORM	Spring Data JPA (Hibernate)
API Docs	Springdoc OpenAPI (Swagger)
Build Tool	Maven
Server	Embedded Tomcat
📁 Project Structure
src/main/java/com/accessiq
│
├── config          # Application & Swagger configuration
├── controller      # REST Controllers
├── dto             # Request / Response DTOs
├── exception       # Global exception handling
├── model           # JPA Entities
├── repository      # Database access layer
├── security        # JWT, filters, security config
├── service         # Business logic
│
└── AccessiqApplication.java

🔐 Authentication Flow

User logs in using credentials

Server generates JWT Access Token + Refresh Token

Client sends JWT in Authorization header

Requests are validated using JWT filters

Role-based access is enforced at API level

⚙️ Configuration
application.properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/accessiq
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

🧪 How to Run Locally
Prerequisites

Java 17 installed and configured

MySQL Server running

Database created:

CREATE DATABASE accessiq;

Run the Application
mvnw.cmd spring-boot:run


Once started, you should see:

Tomcat started on port(s): 8080
Started AccessiqApplication

🌐 API Access
Test Endpoint
GET http://localhost:8080/test

Swagger UI
http://localhost:8080/swagger-ui/index.html

🧠 Key Design Principles

Separation of concerns using layered architecture

Stateless authentication using JWT

Secure API design with role-based restrictions

Scalable workflow & approval modeling

Production-ready coding practices

📌 Future Enhancements

Email notifications for approvals

Pagination & filtering APIs

Deployment on AWS / Render

Docker & CI/CD integration

Frontend integration (React / Angular)

👤 Author

Siddharth Singh
Backend Developer | Java | Spring Boot | REST APIs

📄 License

This project is for learning and demonstration purposes.
