package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mpin_records")
public class MpinRecordEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String mpin;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked;

    public MpinRecordEntity() {
    }

    public MpinRecordEntity(String userId, String mpin) {
        this.userId = userId;
        this.mpin = mpin;
        this.failedAttempts = 0;
        this.isLocked = false;
    }

    public MpinRecordEntity(String userId, String mpin, int failedAttempts, boolean isLocked) {
        this.userId = userId;
        this.mpin = mpin;
        this.failedAttempts = failedAttempts;
        this.isLocked = isLocked;
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

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
