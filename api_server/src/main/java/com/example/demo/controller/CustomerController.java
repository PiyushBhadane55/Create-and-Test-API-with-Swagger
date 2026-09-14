package com.example.demo.controller;

import com.example.demo.model.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "Endpoints for managing customer records")
public class CustomerController {

    private final Map<Long, Customer> customerMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public CustomerController() {
        // Seed initial data
        long id1 = idGenerator.incrementAndGet();
        customerMap.put(id1, new Customer(id1, "Alice Smith", "alice@example.com", "+1-555-0101"));

        long id2 = idGenerator.incrementAndGet();
        customerMap.put(id2, new Customer(id2, "Bob Jones", "bob@example.com", "+1-555-0102"));
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of all registered customers.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of customers")
    @GetMapping
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customerMap.values());
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a single customer by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @Parameter(description = "ID of the customer to fetch", required = true)
            @PathVariable Long id) {
        Customer customer = customerMap.get(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Create a new customer", description = "Creates a new customer record with auto-generated ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer successfully created",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Parameter(description = "Customer details to create", required = true)
            @Valid @RequestBody Customer customer) {
        Long newId = idGenerator.incrementAndGet();
        customer.setId(newId);
        customerMap.put(newId, customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @Operation(summary = "Update an existing customer", description = "Updates customer details for the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer successfully updated",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "ID of the customer to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated customer details", required = true)
            @Valid @RequestBody Customer customerDetails) {
        if (!customerMap.containsKey(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        customerDetails.setId(id);
        customerMap.put(id, customerDetails);
        return ResponseEntity.ok(customerDetails);
    }

    @Operation(summary = "Delete a customer", description = "Deletes a customer record by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to delete", required = true)
            @PathVariable Long id) {
        if (customerMap.remove(id) != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
