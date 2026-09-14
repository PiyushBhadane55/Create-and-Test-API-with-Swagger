package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for verifying user MPIN")
public class MpinVerifyRequest {

    @Schema(description = "Unique User ID or Account Number", example = "USER1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "User's 4 or 6-digit MPIN", example = "4829", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "MPIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "MPIN must be 4 to 6 numeric digits")
    private String mpin;

    public MpinVerifyRequest() {
    }

    public MpinVerifyRequest(String userId, String mpin) {
        this.userId = userId;
        this.mpin = mpin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }
}
