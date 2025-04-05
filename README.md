# Online Bookstore

A Spring Boot application for an online bookstore with user authentication, book management, and order processing.

## Features

- User authentication and authorization with JWT
- Book catalog with search and filtering capabilities
- Shopping cart functionality with real-time updates
- Order management system with status tracking
- Admin dashboard for inventory management and order processing
- Secure API endpoints with role-based access control

## Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Security with JWT authentication
- Spring Data JPA for database operations
- Hibernate ORM
- RESTful API design
- H2 Database (for development)
- MySQL/PostgreSQL (for production)
- Maven for dependency management
- JUnit and Mockito for testing

## Architecture

The application follows a layered architecture:

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Manages data access
- **Model Layer**: Defines entity classes
- **DTO Layer**: Data transfer objects for API communication
- **Security Layer**: Handles authentication and authorization

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and get JWT token

### Books

- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `GET /api/books/search?query={query}` - Search books
- `GET /api/books/category/{category}` - Get books by category
- `POST /api/books` - Add a new book (Admin only)
- `PUT /api/books/{id}` - Update a book (Admin only)
- `DELETE /api/books/{id}` - Delete a book (Admin only)

### Orders

- `GET /api/orders` - Get user's orders
- `POST /api/orders/checkout` - Create a new order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/all` - Get all orders (Admin only)
- `PUT /api/orders/{id}/status` - Update order status (Admin only)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL/PostgreSQL (optional, can use H2 for development)

### Installation

1. Clone the repository

```bash
git clone https://github.com/shri-raj/online_bookstore.git
cd online_bookstore
```
