package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for changing an existing MPIN")
public class MpinChangeRequest {

    @Schema(description = "Unique User ID or Account Number", example = "USER1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "Current MPIN", example = "4829", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Current MPIN is required")
    private String oldMpin;

    @Schema(description = "New MPIN", example = "7392", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New MPIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "New MPIN must be 4 to 6 numeric digits")
    private String newMpin;

    @Schema(description = "Confirm New MPIN", example = "7392", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm New MPIN is required")
    private String confirmNewMpin;

    public MpinChangeRequest() {
    }

    public MpinChangeRequest(String userId, String oldMpin, String newMpin, String confirmNewMpin) {
        this.userId = userId;
        this.oldMpin = oldMpin;
        this.newMpin = newMpin;
        this.confirmNewMpin = confirmNewMpin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOldMpin() {
        return oldMpin;
    }

    public void setOldMpin(String oldMpin) {
        this.oldMpin = oldMpin;
    }

    public String getNewMpin() {
        return newMpin;
    }

    public void setNewMpin(String newMpin) {
        this.newMpin = newMpin;
    }

    public String getConfirmNewMpin() {
        return confirmNewMpin;
    }

    public void setConfirmNewMpin(String confirmNewMpin) {
        this.confirmNewMpin = confirmNewMpin;
    }
}
