package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for resetting locked MPIN using reset token/OTP")
public class MpinResetRequest {

    @Schema(description = "Unique User ID or Account Number", example = "USER1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "Reset Token or OTP", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Reset token / OTP is required")
    private String resetToken;

    @Schema(description = "New MPIN", example = "9182", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New MPIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "New MPIN must be 4 to 6 numeric digits")
    private String newMpin;

    @Schema(description = "Confirm New MPIN", example = "9182", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm New MPIN is required")
    private String confirmNewMpin;

    public MpinResetRequest() {
    }

    public MpinResetRequest(String userId, String resetToken, String newMpin, String confirmNewMpin) {
        this.userId = userId;
        this.resetToken = resetToken;
        this.newMpin = newMpin;
        this.confirmNewMpin = confirmNewMpin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
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
