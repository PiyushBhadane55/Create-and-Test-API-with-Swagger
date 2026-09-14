package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic response payload for MPIN operations")
public class MpinResponse {

    @Schema(description = "Indicates if operation succeeded", example = "true")
    private boolean success;

    @Schema(description = "Response status code or description", example = "MPIN_VERIFIED_SUCCESS")
    private String status;

    @Schema(description = "Human readable response message", example = "MPIN verified successfully")
    private String message;

    @Schema(description = "Number of failed attempts remaining before account lock", example = "2")
    private Integer remainingAttempts;

    @Schema(description = "Indicates whether user MPIN status is locked", example = "false")
    private boolean accountLocked;

    public MpinResponse() {
    }

    public MpinResponse(boolean success, String status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public MpinResponse(boolean success, String status, String message, Integer remainingAttempts, boolean accountLocked) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.remainingAttempts = remainingAttempts;
        this.accountLocked = accountLocked;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
}
