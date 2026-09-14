package com.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for setting up a new MPIN")
public class MpinSetupRequest {

    @Schema(description = "Unique User ID or Account Number", example = "USER1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "4 or 6-digit MPIN (numeric only, non-sequential and non-repeating)", example = "4829", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "MPIN is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "MPIN must be 4 to 6 numeric digits")
    private String mpin;

    @Schema(description = "Confirmation of MPIN (must match mpin)", example = "4829", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm MPIN is required")
    private String confirmMpin;

    public MpinSetupRequest() {
    }

    public MpinSetupRequest(String userId, String mpin, String confirmMpin) {
        this.userId = userId;
        this.mpin = mpin;
        this.confirmMpin = confirmMpin;
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

    public String getConfirmMpin() {
        return confirmMpin;
    }

    public void setConfirmMpin(String confirmMpin) {
        this.confirmMpin = confirmMpin;
    }
}
