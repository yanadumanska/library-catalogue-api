# Library Catalogue RESTful API Design

---

## 1. Functional and Non-Functional Requirements

### Functional Requirements

- **Book Management:** Create, retrieve, update, and delete books in the catalogue
- **Author Management:** Manage author information and their relationships with books
- **Category Management:** Organize books into hierarchical categories
- **Search & Discovery:** Allow users to search books by various criteria (title, author, ISBN, category)
- **Borrowing Management:** Track book availability and borrowing status
- **User Management:** Handle library patron registration and profile management
- **Reviews & Ratings:** Allow patrons to review and rate books
- **Reservation System:** Enable patrons to reserve currently borrowed books

### Non-Functional Requirements

- **Performance:** API responses should be under 200ms for 95% of requests
- **Scalability:** System should handle up to 10,000 concurrent users
- **Security:** All endpoints must use HTTPS; authentication via JWT tokens
- **Availability:** 99.9% uptime with graceful degradation
- **Caching:** Implement ETags and Cache-Control headers for appropriate resources
- **Rate Limiting:** 1000 requests per hour per API key
- **Pagination:** All list endpoints must support cursor-based pagination
- **Versioning:** API versioning through URL path (`/api/v1/`)

---

## 2. Model Description

### Core Entities

#### Book

```json
{
  "id": "UUID",
  "isbn": "string (unique)",
  "title": "string",
  "subtitle": "string (optional)",
  "description": "string",
  "publication_date": "date",
  "publisher": "string",
  "page_count": "integer",
  "language": "string (ISO 639-1)",
  "format": "enum [HARDCOVER, PAPERBACK, EBOOK, AUDIOBOOK]",
  "status": "enum [AVAILABLE, BORROWED, RESERVED, MAINTENANCE]",
  "shelf_location": "string",
  "total_copies": "integer",
  "available_copies": "integer",
  "authors": ["Author reference"],
  "categories": ["Category reference"],
  "average_rating": "decimal (0-5)",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

#### Author

```json
{
  "id": "UUID",
  "first_name": "string",
  "last_name": "string",
  "biography": "string",
  "birth_date": "date (optional)",
  "death_date": "date (optional)",
  "nationality": "string",
  "website": "url (optional)",
  "books": ["Book reference"],
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

#### Category

```json
{
  "id": "UUID",
  "name": "string",
  "description": "string",
  "parent_category": "Category reference (optional)",
  "subcategories": ["Category reference"],
  "books_count": "integer",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

#### User (Patron)

```json
{
  "id": "UUID",
  "username": "string (unique)",
  "email": "string (unique)",
  "full_name": "string",
  "membership_type": "enum [BASIC, PREMIUM, STUDENT, FACULTY]",
  "membership_status": "enum [ACTIVE, SUSPENDED, EXPIRED]",
  "max_borrow_limit": "integer",
  "current_borrows": "integer",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

#### Borrowing Record

```json
{
  "id": "UUID",
  "book": "Book reference",
  "user": "User reference",
  "borrow_date": "datetime",
  "due_date": "datetime",
  "return_date": "datetime (optional)",
  "status": "enum [ACTIVE, RETURNED, OVERDUE]",
  "renewal_count": "integer",
  "fine_amount": "decimal (optional)",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

#### Review

```json
{
  "id": "UUID",
  "book": "Book reference",
  "user": "User reference",
  "rating": "integer (1-5)",
  "title": "string",
  "content": "string",
  "spoiler_flag": "boolean",
  "created_at": "datetime",
  "updated_at": "datetime"
}
```

---

## 3. REST API Description

### Base URL

```
https://api.library-catalogue.com/api/v1
```

### Authentication Header

All endpoints (except registration and login) require:

```
Authorization: Bearer <JWT_TOKEN>
```

### Standard Response Envelope

```json
{
  "data": {},
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z",
    "version": "1.0"
  },
  "links": {
    "self": "current_resource_url",
    "next": "next_page_url",
    "prev": "previous_page_url"
  }
}
```

---

## 4. API Endpoints

### 4.1 Books Resource

**Collection:** `/books`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/books` | List all books (paginated) | No |
| GET | `/books/{bookId}/authors` | Get book authors | No |
| GET | `/books/{bookId}/categories` | Get book categories | No |
| POST | `/books` | Create a new book | Yes (Admin/Librarian) |
| GET | `/books/{bookId}` | Get book details | No |
| PUT | `/books/{bookId}` | Update entire book | Yes (Admin/Librarian) |
| DELETE | `/books/{bookId}` | Remove book | Yes (Admin) |

#### `GET /books` — Query Parameters

- `page` (integer, default: 1)
- `limit` (integer, default: 20, max: 100)
- `search` (string, full-text search on title, ISBN, description)
- `author` (string, author name partial match)
- `category` (string, category name)
- `format` (enum: `HARDCOVER`, `PAPERBACK`, `EBOOK`, `AUDIOBOOK`)
- `status` (enum: `AVAILABLE`, `BORROWED`, `RESERVED`)
- `language` (string, ISO 639-1)
- `min_rating` (decimal, 0–5)
- `sort` (string, format: `field:direction`, e.g. `"title:asc,publication_date:desc"`)
- `published_after` (date)
- `published_before` (date)

#### Response Headers

```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: public, max-age=300
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
Link: </books?page=2&limit=20>; rel="next",
      </books?page=1&limit=20>; rel="first",
      </books?page=50&limit=20>; rel="last"
X-Total-Count: 1000
X-Page: 1
X-Page-Size: 20
```

#### `POST /books` — Request Body

```json
{
  "isbn": "978-0-7475-3269-9",
  "title": "Harry Potter and the Philosopher's Stone",
  "subtitle": null,
  "description": "A young wizard's journey begins...",
  "publication_date": "1997-06-26",
  "publisher": "Bloomsbury",
  "page_count": 223,
  "language": "en",
  "format": "PAPERBACK",
  "shelf_location": "FIC-ROW-01",
  "total_copies": 5,
  "available_copies": 5,
  "author_ids": ["uuid-1", "uuid-2"],
  "category_ids": ["uuid-3"]
}
```

#### Response

```http
HTTP/1.1 201 Created
Location: /books/550e8400-e29b-41d4-a716-446655440000
```

#### Sub-resources

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/books/{bookId}/reviews` | Get reviews for a book |
| POST | `/books/{bookId}/reviews` | Add review to book |
| GET | `/books/{bookId}/borrowing-history` | Get borrowing history |
| GET | `/books/{bookId}/availability` | Check current availability |

---

### 4.2 Authors Resource

**Collection:** `/authors`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/authors` | List authors (paginated) | No |
| GET | `/authors/{authorId}/books` | Get books by author | No |
| POST | `/authors` | Create author | Yes (Admin/Librarian) |
| GET | `/authors/{authorId}` | Get author details | No |
| PUT | `/authors/{authorId}` | Update author | Yes (Admin/Librarian) |
| DELETE | `/authors/{authorId}` | Remove author | Yes (Admin) |

#### `GET /authors` — Query Parameters

- `page`, `limit` (pagination)
- `search` (string, search by name)
- `nationality` (string)
- `sort` (string, e.g. `"last_name:asc"`)

#### Sub-resource

`GET /authors/{authorId}/books` — Get all books by this author

---

### 4.3 Categories Resource

**Collection:** `/categories`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/categories` | List categories | No |
| GET | `/categories/{categoryId}/books` | Get books by category | No |
| POST | `/categories` | Create category | Yes (Admin) |
| GET | `/categories/{categoryId}` | Get category details | No |
| PUT | `/categories/{categoryId}` | Update category | Yes (Admin) |
| DELETE | `/categories/{categoryId}` | Delete category | Yes (Admin) |

#### `GET /categories` — Special Features

- Returns hierarchical tree structure
- Parameter `flat=true` to flatten the hierarchy
- `parent_id` parameter to filter by parent category

#### Sub-resource

`GET /categories/{categoryId}/books` — Books in this category

---

### 4.4 Users Resource (Patrons)

**Collection:** `/users`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users/register` | Register new patron | No |
| POST | `/users/login` | Login and get JWT | No |
| POST | `/users/refresh-token` | Refresh JWT token | Yes (Refresh Token) |
| GET | `/users/me` | Get current user profile | Yes |
| PUT | `/users/me` | Update current user profile | Yes |
| GET | `/users` | List all users | Yes (Admin) |
| GET | `/users/{userId}` | Get specific user | Yes (Admin) |
| PATCH | `/users/{userId}/status` | Update user status | Yes (Admin) |

#### `POST /users/register`

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "full_name": "John Doe",
  "membership_type": "BASIC"
}
```

#### `POST /users/login`

```json
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Login Response

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "id": "uuid",
    "username": "john_doe",
    "email": "john@example.com"
  },
  "links": {
    "profile": "/users/me",
    "borrowings": "/borrowings"
  }
}
```

#### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "john@example.com",
    "role": "PATRON",
    "iat": 1516239022,
    "exp": 1516242622,
    "jti": "unique-token-id"
  }
}
```

---

### 4.5 Borrowings Resource

**Collection:** `/borrowings`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/borrowings` | List current user's borrowings | Yes |
| POST | `/borrowings` | Borrow a book | Yes |
| GET | `/borrowings/{borrowingId}` | Get borrowing details | Yes |
| POST | `/borrowings/{borrowingId}/renew` | Renew a borrowing | Yes |
| POST | `/borrowings/{borrowingId}/return` | Return a book | Yes |

#### `POST /borrowings`

```json
{
  "book_id": "book-uuid",
  "expected_duration_days": 14
}
```

#### Query Parameters for `GET /borrowings`

- `status` (`ACTIVE`, `RETURNED`, `OVERDUE`)
- `user_id` (Admin only)
- `book_id` (filter by book)

---

### 4.6 Reservations Resource

**Collection:** `/reservations`

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/reservations` | List user's reservations | Yes |
| POST | `/reservations` | Reserve a book | Yes |
| DELETE | `/reservations` | Cancel reservation | Yes |

#### `POST /reservations`

```json
{
  "book_id": "book-uuid",
  "notification_preference": "EMAIL"
}
```

---

### 4.7 Reviews Resource

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/books/{bookId}/reviews` | Add review | Yes |
| GET | `/books/{bookId}/reviews` | Get book reviews | No |
| PUT | `/reviews/{reviewId}` | Update review | Yes (Owner) |
| DELETE | `/reviews/{reviewId}` | Delete review | Yes (Owner/Admin) |

---

## 6. Status Codes

| Status Code | Meaning | Usage |
|-------------|---------|-------|
| 200 | OK | Successful request — GET, PUT,  operations |
| 201 | Created | Resource created — POST operations |
| 204 | No Content | Successful deletion — DELETE operations |
| 304 | Not Modified | Resource not modified — Conditional GET with ETag/If-None-Match |
| 400 | Bad Request | Invalid request data — Validation errors |
| 401 | Unauthorized | Missing/invalid authentication — No token or expired token |
| 403 | Forbidden | Insufficient permissions — Valid token but wrong role |
| 404 | Not Found | Resource doesn't exist — Invalid ID in URL |
| 409 | Conflict | Resource state conflict — Duplicate ISBN, book already borrowed |
| 422 | Unprocessable Entity | Semantic errors — Business rule violations |
| 429 | Too Many Requests | Rate limit exceeded — Exceeded 1000 req/hour |
| 500 | Internal Server Error | Unexpected server error — Unhandled exceptions |
| 503 | Service Unavailable | Service temporarily down — Maintenance mode |

#### Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid book data provided",
    "details": [
      {
        "field": "isbn",
        "message": "ISBN must be a valid 13-digit number"
      },
      {
        "field": "page_count",
        "message": "Page count must be greater than 0"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z",
    "request_id": "req-uuid"
  }
}
```

---

## 7. Caching Strategy

### Cacheable Resources (with ETags and Cache-Control)

| Resource | Cache Duration | Cache-Control Header |
|----------|----------------|----------------------|
| `GET /books` (list) | 5 minutes | `public, max-age=300` |
| `GET /books/{id}` | 10 minutes | `public, max-age=600` |
| `GET /authors` | 15 minutes | `public, max-age=900` |
| `GET /categories` | 30 minutes | `public, max-age=1800` |
| `GET /books/{id}/reviews` | 5 minutes | `public, max-age=300` |

### Non-Cacheable Resources

- `POST`/`PUT`/`DELETE` operations (mutations)
- User profile (`GET /users/me`) — Contains personal data
- Borrowings — Time-sensitive availability data
- Reservations — Real-time state required
- Login/Register — Authentication endpoints

### Caching Implementation

**Request:**

```http
GET /books/123 HTTP/1.1
If-None-Match: "abc123"
```

**Response (Not Modified):**

```http
HTTP/1.1 304 Not Modified
ETag: "abc123"
Cache-Control: public, max-age=600
```

**Response (Modified):**

```http
HTTP/1.1 200 OK
ETag: "def456"
Cache-Control: public, max-age=600
```

---

## 8. Complete Operation Example

### Borrow a Book — Full Flow

#### Step 1: Check Book Availability

```http
GET /books/550e8400-e29b-41d4-a716-446655440000/availability
Authorization: Bearer <JWT>
```

**Response 200:**

```json
{
  "data": {
    "book_id": "550e8400...",
    "status": "AVAILABLE",
    "available_copies": 3,
    "total_copies": 5,
    "next_available_date": null
  }
}
```

#### Step 2: Borrow the Book

```http
POST /borrowings
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "book_id": "550e8400-e29b-41d4-a716-446655440000",
  "expected_duration_days": 14
}
```

**Response 201:**

```http
Location: /borrowings/660e8400...
```

```json
{
  "data": {
    "id": "660e8400...",
    "book_title": "Clean Code",
    "due_date": "2024-01-29T10:30:00Z",
    "status": "ACTIVE",
    "_links": {
      "renew": {"href": "/borrowings/660e8400.../renew"},
      "return": {"href": "/borrowings/660e8400.../return"}
    }
  }
}
```

#### Step 3: Return the Book

```http
POST /borrowings/660e8400.../return
Authorization: Bearer <JWT>
```

**Response 200:**

```json
{
  "data": {
    "id": "660e8400...",
    "status": "RETURNED",
    "return_date": "2024-01-20T15:00:00Z",
    "fine_amount": 0
  }
}
```

---

## 9. API Versioning and Documentation

### OpenAPI Specification

The API is fully documented using OpenAPI 3.0 specification, available at:

```
GET /api/v1/docs/openapi.json
```

### API Health Check

```
GET /api/v1/health
```

**Response 200:**

```json
{
  "status": "healthy",
  "version": "1.0.0",
  "uptime": "15d 4h 23m",
  "database": "connected",
  "cache": "operational"
}
