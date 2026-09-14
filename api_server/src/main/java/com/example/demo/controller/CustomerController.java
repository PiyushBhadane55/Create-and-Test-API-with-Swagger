package com.example.demo.controller;

import com.example.demo.entity.CustomerEntity;
import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "Endpoints for managing customer records with PostgreSQL persistence")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of all registered customers from PostgreSQL database.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of customers")
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
        Optional<CustomerEntity> entity = customerRepository.findById(id);
        return entity.map(customerEntity -> ResponseEntity.ok(toDto(customerEntity)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Create a new customer", description = "Creates a new customer record stored in PostgreSQL database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer successfully created",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Parameter(description = "Customer details to create", required = true)
            @Valid @RequestBody Customer customer) {
        CustomerEntity entity = new CustomerEntity(customer.getName(), customer.getEmail(), customer.getPhone());
        CustomerEntity saved = customerRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @Operation(summary = "Update an existing customer", description = "Updates customer details for the given ID in database.")
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
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        CustomerEntity entity = new CustomerEntity(id, customerDetails.getName(), customerDetails.getEmail(), customerDetails.getPhone());
        CustomerEntity updated = customerRepository.save(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(summary = "Delete a customer", description = "Deletes a customer record from database by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to delete", required = true)
            @PathVariable Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private Customer toDto(CustomerEntity entity) {
        return new Customer(entity.getId(), entity.getName(), entity.getEmail(), entity.getPhone());
    }
}
