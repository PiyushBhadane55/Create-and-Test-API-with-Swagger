# Customer API Sequence Diagrams

This document contains detailed **Mermaid Sequence Diagrams** illustrating the complete lifecycle of request execution, data validation, Swagger UI auto-documentation, and automated E2E testing in the Spring Boot Customer API project.

---

## 1. 🔄 Complete REST API CRUD Operations Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Client as HTTP Client / Postman
    participant Dispatcher as Spring DispatcherServlet
    participant Controller as CustomerController
    participant Model as Customer Model (@Valid)
    participant Store as ConcurrentHashMap Store

    %% --- GET ALL CUSTOMERS ---
    rect rgb(240, 248, 255)
    note over Client, Store: 1. GET /api/customers (Fetch All Customers)
    Client->>Dispatcher: GET /api/customers
    Dispatcher->>Controller: Route to getAllCustomers()
    Controller->>Store: Read all values from customerMap
    Store-->>Controller: Return Collection<Customer>
    Controller-->>Client: 200 OK + JSON Array [Customer]
    end

    %% --- GET CUSTOMER BY ID ---
    rect rgb(245, 245, 245)
    note over Client, Store: 2. GET /api/customers/{id} (Fetch Single Customer)
    Client->>Dispatcher: GET /api/customers/1
    Dispatcher->>Controller: Route to getCustomerById(1)
    Controller->>Store: customerMap.get(1)
    alt Customer Found
        Store-->>Controller: Return Customer object
        Controller-->>Client: 200 OK + JSON Customer Payload
    else Customer Not Found
        Store-->>Controller: null
        Controller-->>Client: 404 Not Found
    end
    end

    %% --- POST CREATE CUSTOMER ---
    rect rgb(240, 255, 240)
    note over Client, Store: 3. POST /api/customers (Create New Customer)
    Client->>Dispatcher: POST /api/customers {name, email, phone}
    Dispatcher->>Controller: Route to createCustomer(@Valid Customer)
    Controller->>Model: Execute Jakarta Bean Validation
    alt Payload Invalid (e.g. Empty Name or Invalid Email)
        Model-->>Controller: Validation Errors
        Controller-->>Client: 400 Bad Request
    else Payload Valid
        Controller->>Store: AtomicLong.incrementAndGet() -> Generate new ID
        Controller->>Store: customerMap.put(newId, customer)
        Store-->>Controller: Object Saved
        Controller-->>Client: 201 Created + JSON Customer Payload
    end
    end

    %% --- PUT UPDATE CUSTOMER ---
    rect rgb(255, 250, 240)
    note over Client, Store: 4. PUT /api/customers/{id} (Update Customer)
    Client->>Dispatcher: PUT /api/customers/1 {name, email, phone}
    Dispatcher->>Controller: Route to updateCustomer(1, @Valid Customer)
    Controller->>Model: Execute Jakarta Bean Validation
    alt Validation Failure
        Model-->>Controller: Validation Errors
        Controller-->>Client: 400 Bad Request
    else Validation Success
        Controller->>Store: customerMap.containsKey(1)
        alt ID Exists
            Controller->>Store: customerMap.put(1, updatedCustomer)
            Store-->>Controller: Updated successfully
            Controller-->>Client: 200 OK + JSON Customer Payload
        else ID Does Not Exist
            Controller-->>Client: 404 Not Found
        end
    end
    end

    %% --- DELETE CUSTOMER ---
    rect rgb(255, 240, 245)
    note over Client, Store: 5. DELETE /api/customers/{id} (Delete Customer)
    Client->>Dispatcher: DELETE /api/customers/1
    Dispatcher->>Controller: Route to deleteCustomer(1)
    Controller->>Store: customerMap.remove(1)
    alt Remove Success
        Store-->>Controller: Removed Customer object
        Controller-->>Client: 204 No Content
    else ID Not Found
        Store-->>Controller: null
        Controller-->>Client: 404 Not Found
    end
    end
```

---

## 2. 📖 OpenAPI Specification & Swagger UI Rendering Flow

```mermaid
sequenceDiagram
    autonumber
    actor Developer as Developer / Browser
    participant Browser as Web Browser Window
    participant UI as Swagger UI Frontend Engine
    participant Springdoc as Springdoc OpenAPI Library
    participant Reflection as Spring Controller Inspection

    Developer->>Browser: Navigate to http://localhost:8080/swagger-ui.html
    Browser->>Springdoc: GET /swagger-ui.html (Redirect to index.html)
    Springdoc-->>Browser: Serve Swagger UI HTML, CSS & JS Assets
    Browser->>UI: Initialize Swagger UI Application
    UI->>Springdoc: Async GET /v3/api-docs (Fetch OpenAPI Spec)
    Springdoc->>Reflection: Scan @RestController, @Tag, @Operation annotations
    Reflection-->>Springdoc: Return complete API schema metadata
    Springdoc-->>UI: Serve JSON OpenAPI 3.0 Document
    UI->>UI: Parse OpenAPI JSON & Render API endpoints dynamically
    UI-->>Browser: Display Interactive Swagger UI Dashboard

    note over Developer, UI: Interactive "Try It Out" Execution Flow
    Developer->>UI: Click "Try it out" -> Fill parameters -> Click "Execute"
    UI->>Springdoc: Send HTTP Request to /api/customers
    Springdoc-->>UI: HTTP Response (Status 200/201/204 + Headers + Body)
    UI-->>Developer: Render Request URL, Curl command, Status Code & Response Payload
```

---

## 3. 🧪 Playwright Automated E2E Test Execution Flow

```mermaid
sequenceDiagram
    autonumber
    actor Tester as Test Engineer / CI Pipeline
    participant Runner as Playwright Test Runner
    participant APISpec as tests/api.spec.js
    participant UISpec as tests/swagger-ui.spec.js
    participant App as Spring Boot Application (Port 8080)
    participant BrowserCtx as Playwright Headless Browser

    Tester->>Runner: Execute `npx playwright test`
    
    %% API Tests
    rect rgb(240, 248, 255)
    note over Runner, App: Phase A: API Endpoint Integration Testing
    Runner->>APISpec: Run REST API spec tests
    APISpec->>App: GET /api/customers
    App-->>APISpec: 200 OK [Initial Customers]
    APISpec->>App: POST /api/customers (New Customer)
    App-->>APISpec: 201 Created {id: 3, name: ...}
    APISpec->>App: PUT /api/customers/1 (Update Customer)
    App-->>APISpec: 200 OK {id: 1, name: updated...}
    APISpec->>App: DELETE /api/customers/3
    App-->>APISpec: 204 No Content
    APISpec-->>Runner: API Test Suite Passed (5/5 tests)
    end

    %% UI Tests
    rect rgb(255, 250, 240)
    note over Runner, BrowserCtx: Phase B: Swagger UI Frontend Verification
    Runner->>UISpec: Run Swagger UI browser tests
    UISpec->>BrowserCtx: Launch Chromium Context -> Open /swagger-ui.html
    BrowserCtx->>App: Request Swagger UI & /v3/api-docs
    App-->>BrowserCtx: 200 OK (Swagger assets & spec)
    BrowserCtx->>BrowserCtx: Render DOM elements (.title, .version, .opblock)
    UISpec->>BrowserCtx: Assert `.title` contains "Customer Management API"
    UISpec->>BrowserCtx: Assert HTTP method badges (GET, POST, PUT, DELETE) are visible
    UISpec-->>Runner: UI Test Suite Passed (2/2 tests)
    end

    Runner-->>Tester: All E2E Tests Executed Successfully (7/7 Passed)
```
