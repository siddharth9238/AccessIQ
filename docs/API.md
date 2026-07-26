# API Documentation

## Overview

AccessIQ provides a RESTful API for workflow management. All endpoints are versioned under `/api/v1`.

## Authentication

All protected endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

## Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/auth/login"
}
```

## Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Access denied |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 422 | Unprocessable Entity - Validation error |
| 500 | Internal Server Error |

## Authentication Endpoints

### POST /api/v1/auth/login

Authenticate a user and receive tokens.

**Request:**
```json
{
  "email": "admin@accessiq.com",
  "password": "Admin@123456"
}
```

**Response:**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
  }
}
```

### POST /api/v1/auth/refresh

Refresh an expired access token.

**Request:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Response:**
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "bm53IHJlZnJlc2ggdG9rZW4..."
  }
}
```

## Request Endpoints

### GET /api/v1/requests

List all requests for the authenticated user.

**Response:**
```json
{
  "data": [
    {
      "id": 1,
      "title": "Leave Request",
      "description": "Vacation leave for 5 days",
      "status": "APPROVED",
      "createdBy": "employee@accessiq.com",
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-16T14:30:00"
    }
  ]
}
```

### POST /api/v1/requests

Create a new request.

**Request:**
```json
{
  "title": "Leave Request",
  "description": "Vacation leave for 5 days"
}
```

**Response:**
```json
{
  "data": {
    "message": "Request created successfully",
    "result": {
      "id": 1,
      "title": "Leave Request",
      "description": "Vacation leave for 5 days",
      "status": "PENDING"
    }
  }
}
```

### POST /api/v1/requests/{id}/approve

Approve a request (MANAGER or ADMIN role).

**Response:**
```json
{
  "data": {
    "message": "Request approved successfully",
    "result": {
      "id": 1,
      "status": "APPROVED"
    }
  }
}
```

### POST /api/v1/requests/{id}/reject

Reject a request (MANAGER or ADMIN role).

**Request:**
```json
{
  "details": "Insufficient documentation provided"
}
```

## Admin Endpoints

### GET /api/v1/admin/users

List all users (ADMIN role required).

### POST /api/v1/admin/users

Create a new user (ADMIN role required).

**Request:**
```json
{
  "email": "newuser@accessiq.com",
  "password": "Password123!",
  "roles": ["EMPLOYEE"]
}
```

### GET /api/v1/admin/workflows

List all workflows.

### POST /api/v1/admin/workflows

Create a new workflow.

**Request:**
```json
{
  "name": "Expense Approval",
  "steps": [
    {
      "approverRole": "MANAGER",
      "stepOrder": 1,
      "slaHours": 24
    },
    {
      "approverRole": "ADMIN",
      "stepOrder": 2,
      "slaHours": 24
    }
  ]
}
```

## Audit Endpoints

### GET /api/v1/audit/logs

List audit logs (ADMIN or AUDITOR role required).

**Query Parameters:**
- `performedBy` - Filter by performer
- `action` - Filter by action type

## Validation Rules

### LoginRequest
- `email`: Required, valid email format, max 100 chars
- `password`: Required, max 100 chars

### CreateUserRequest
- `email`: Required, valid email format, max 100 chars
- `password`: Required, min 8 chars, max 100 chars
- `roles`: Required, non-empty array of valid role names

### RequestCreateRequest
- `title`: Required, max 255 chars
- `description`: Optional, max 2000 chars
- `workflowName`: Optional

## Rate Limiting

Rate limiting is configured to prevent abuse:
- 100 requests per minute per IP
- Exceeded requests return 429 Too Many Requests

## Versioning

The API is versioned using URL path:
```
/api/v1/...
```

Future versions will be available at `/api/v2/...`.