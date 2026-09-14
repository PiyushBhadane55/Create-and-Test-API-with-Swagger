# Spring Boot Customer API with Swagger UI & Playwright Tests

A RESTful Spring Boot application providing full CRUD operations for managing customer records, integrated with OpenAPI 3, interactive Swagger UI documentation, and automated Playwright tests.

---

## 🚀 Features

- **Full CRUD Endpoints**:
  - `GET /api/customers` - List all customers
  - `GET /api/customers/{id}` - Fetch customer by ID
  - `POST /api/customers` - Create a new customer
  - `PUT /api/customers/{id}` - Update an existing customer
  - `DELETE /api/customers/{id}` - Delete a customer by ID
- **Input Validation**: Uses `@Valid`, `@NotBlank`, and `@Email` constraint annotations.
- **Interactive API Documentation**: Auto-generated Swagger UI using `springdoc-openapi`.
- **Automated Playwright Tests**: Node.js Playwright API & UI test suite.
- **In-Memory Thread-Safe Data Store**: Uses `ConcurrentHashMap` and `AtomicLong` for instant testing without database setup.

---

## 🛠️ Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6+
- **Node.js**: v18+ (for running Playwright tests)

---

## 🏁 Getting Started

### 1. Compile & Run the Spring Boot Server
Navigate to the `api_server` directory:
```bash
cd api_server
mvn clean compile
mvn spring-boot:run
```
The server will start on **`http://localhost:8080`**.

---

## 🎭 Running Playwright Tests

With the Spring Boot server running on port `8080`, execute:

```bash
# Install node dependencies
npm install

# Run Playwright test suite
npx playwright test
```

### Playwright Test Coverage:
- `tests/api.spec.js`: Validates `GET`, `POST`, `PUT`, and `DELETE` HTTP endpoints.
- `tests/swagger-ui.spec.js`: Validates Swagger UI interface rendering.

---

## 📖 Swagger UI & API Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI v3 JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🧪 Postman Collection

📄 [`Customer_API.postman_collection.json`](./Customer_API.postman_collection.json)

### Importing into Postman:
1. Open Postman -> Click **Import**.
2. Select `Customer_API.postman_collection.json`.

---

## 📂 Project Structure

```text
api_server
├── pom.xml
├── package.json
├── playwright.config.js
├── README.md
├── ARCHITECTURE.md
├── Customer_API.postman_collection.json
├── tests
│   ├── api.spec.js
│   └── swagger-ui.spec.js
└── src
    └── main
        ├── java
        │   └── com/example/demo
        │       ├── DemoApplication.java
        │       ├── config
        │       │   └── OpenApiConfig.java
        │       ├── controller
        │       │   └── CustomerController.java
        │       └── model
        │           └── Customer.java
        └── resources
            └── application.properties
```
