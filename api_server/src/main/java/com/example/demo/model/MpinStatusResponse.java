package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload for checking user MPIN status")
public class MpinStatusResponse {

    @Schema(description = "Unique User ID", example = "USER1001")
    private String userId;

    @Schema(description = "Indicates whether MPIN has been set up", example = "true")
    @JsonProperty("isMpinSet")
    private boolean isMpinSet;

    @Schema(description = "Indicates whether account is locked due to failed attempts", example = "false")
    @JsonProperty("isLocked")
    private boolean isLocked;

    @Schema(description = "Current consecutive failed attempt count", example = "0")
    private int failedAttempts;

    @Schema(description = "Maximum allowed failed attempts before lock", example = "3")
    private int maxAttempts;

    public MpinStatusResponse() {
    }

    public MpinStatusResponse(String userId, boolean isMpinSet, boolean isLocked, int failedAttempts, int maxAttempts) {
        this.userId = userId;
        this.isMpinSet = isMpinSet;
        this.isLocked = isLocked;
        this.failedAttempts = failedAttempts;
        this.maxAttempts = maxAttempts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonProperty("isMpinSet")
    public boolean isMpinSet() {
        return isMpinSet;
    }

    public void setMpinSet(boolean mpinSet) {
        isMpinSet = mpinSet;
    }

    @JsonProperty("isLocked")
    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
