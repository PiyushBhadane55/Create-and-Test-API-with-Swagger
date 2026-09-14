# Customer & MPIN API Sequence Diagrams

This document contains detailed **Mermaid Sequence Diagrams** illustrating the complete lifecycle of request execution, data validation, Swagger UI auto-documentation, MPIN verification flows, and automated testing in the Spring Boot project.

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

## 2. 🔐 MPIN Setup, Verification & 3-Failure Account Lockout Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor User as Mobile App / Client
    participant Dispatcher as Spring DispatcherServlet
    participant Controller as MpinController
    participant Validator as Weak PIN Rules Engine
    participant Store as ConcurrentHashMap<String, MpinRecord>

    %% --- MPIN SETUP ---
    rect rgb(240, 255, 240)
    note over User, Store: Step 1: MPIN Setup (/api/mpin/setup)
    User->>Dispatcher: POST /api/mpin/setup {userId, mpin: "8419", confirmMpin: "8419"}
    Dispatcher->>Controller: setupMpin()
    Controller->>Validator: Check isWeakMpin("8419")
    alt Weak MPIN (e.g. 1234 or 1111)
        Validator-->>Controller: Weak PIN Flagged
        Controller-->>User: 400 Bad Request (WEAK_MPIN)
    else Valid MPIN
        Controller->>Store: Store new MpinRecord (failedAttempts=0, isLocked=false)
        Store-->>Controller: MpinRecord Saved
        Controller-->>User: 201 Created (MPIN_SETUP_SUCCESS)
    end
    end

    %% --- MPIN VERIFICATION & ATTEMPTS ---
    rect rgb(255, 250, 240)
    note over User, Store: Step 2: MPIN Verification (/api/mpin/verify)
    User->>Dispatcher: POST /api/mpin/verify {userId, mpin: "9999"}
    Dispatcher->>Controller: verifyMpin()
    Controller->>Store: Lookup MpinRecord by userId
    alt Wrong MPIN (Attempt #1 & #2)
        Controller->>Store: Increment failedAttempts (1 -> 2)
        Controller-->>User: 401 Unauthorized {status: "INVALID_MPIN", remainingAttempts: 2}
    else Correct MPIN
        Controller->>Store: Reset failedAttempts = 0
        Controller-->>User: 200 OK {status: "MPIN_VERIFIED_SUCCESS", remainingAttempts: 3}
    end
    end

    %% --- 3RD FAILURE LOCKOUT ---
    rect rgb(255, 240, 245)
    note over User, Store: Step 3: Account Lockout on 3rd Failure
    User->>Dispatcher: POST /api/mpin/verify {userId, mpin: "9999"} (3rd Wrong Attempt)
    Dispatcher->>Controller: verifyMpin()
    Controller->>Store: Increment failedAttempts (3/3) -> Set isLocked = true
    Controller-->>User: 423 Locked {status: "ACCOUNT_LOCKED", remainingAttempts: 0, accountLocked: true}
    
    note over User, Controller: Subsequent Verification Attempt
    User->>Dispatcher: POST /api/mpin/verify {userId, mpin: "8419"} (Correct MPIN while locked)
    Dispatcher->>Controller: verifyMpin()
    Controller-->>User: 423 Locked (Blocked - Reset Token Required)
    end

    %% --- MPIN RESET ---
    rect rgb(240, 248, 255)
    note over User, Store: Step 4: Account Unlock via OTP Reset (/api/mpin/reset)
    User->>Dispatcher: POST /api/mpin/reset {userId, resetToken: "123456", newMpin: "9531"}
    Dispatcher->>Controller: resetMpin()
    Controller->>Store: Set new MPIN "9531", isLocked=false, failedAttempts=0
    Controller-->>User: 200 OK {status: "MPIN_RESET_SUCCESS", accountLocked: false}
    end
```

---

## 3. 📖 OpenAPI Specification & Swagger UI Rendering Flow

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
    Reflection-->>Springdoc: Return complete API schema metadata (Customer API & MPIN API)
    Springdoc-->>UI: Serve JSON OpenAPI 3.0 Document
    UI->>UI: Parse OpenAPI JSON & Render API endpoints dynamically
    UI-->>Browser: Display Interactive Swagger UI Dashboard

    note over Developer, UI: Interactive "Try It Out" Execution Flow
    Developer->>UI: Select MPIN API -> Click "Try it out" -> Enter payload -> "Execute"
    UI->>Springdoc: Send HTTP POST Request to /api/mpin/verify
    Springdoc-->>UI: HTTP Response (Status 200/401/423 + Headers + Body)
    UI-->>Developer: Render Request URL, Curl command, Status Code & Response Payload
```

---

## 4. 🧪 Playwright Automated E2E Test Execution Flow

```mermaid
sequenceDiagram
    autonumber
    actor Tester as Test Engineer / CI Pipeline
    participant Runner as Playwright Test Runner
    participant APISpec as tests/api.spec.js
    participant MPINSpec as tests/mpin.spec.js
    participant App as Spring Boot Application (Port 8080)

    Tester->>Runner: Execute `npx playwright test`
    
    %% Customer API Tests
    rect rgb(240, 248, 255)
    note over Runner, App: Phase A: Customer REST API Integration Testing
    Runner->>APISpec: Run Customer API tests
    APISpec->>App: GET /api/customers & GET /api/customers/1
    App-->>APISpec: 200 OK
    APISpec->>App: POST /api/customers & PUT & DELETE
    App-->>APISpec: 201 Created, 200 OK, 204 No Content
    APISpec-->>Runner: Customer API Spec Passed (5/5 tests)
    end

    %% MPIN API Tests
    rect rgb(255, 250, 240)
    note over Runner, App: Phase B: MPIN Authentication API Testing
    Runner->>MPINSpec: Run MPIN API tests
    MPINSpec->>App: GET /api/mpin/status/USER1001
    App-->>MPINSpec: 200 OK {isMpinSet: true, isLocked: false}
    MPINSpec->>App: POST /api/mpin/setup (Weak PIN "1234")
    App-->>MPINSpec: 400 Bad Request (WEAK_MPIN)
    MPINSpec->>App: POST /api/mpin/verify (Correct MPIN)
    App-->>MPINSpec: 200 OK (MPIN_VERIFIED_SUCCESS)
    MPINSpec->>App: POST /api/mpin/verify (3 Wrong Attempts)
    App-->>MPINSpec: 401 Unauthorized -> 423 Locked
    MPINSpec->>App: POST /api/mpin/reset (OTP Token "123456")
    App-->>MPINSpec: 200 OK (MPIN_RESET_SUCCESS)
    MPINSpec->>App: POST /api/mpin/change (Change MPIN)
    App-->>MPINSpec: 200 OK (MPIN_CHANGED_SUCCESS)
    MPINSpec-->>Runner: MPIN API Spec Passed (8/8 tests)
    end

    Runner-->>Tester: All E2E API Tests Passed (13/13 Passed)
```
