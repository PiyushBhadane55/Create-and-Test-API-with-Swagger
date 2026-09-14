package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Customer Model Information")
public class Customer {

    @Schema(description = "Unique identifier of the customer", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Full name of the customer", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Email address of the customer", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Contact phone number of the customer", example = "+1-555-0199")
    private String phone;

    public Customer() {
    }

    public Customer(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
