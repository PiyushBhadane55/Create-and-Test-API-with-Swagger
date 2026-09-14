-- V2: Create mpin_records table & insert seed record
CREATE TABLE mpin_records (
    user_id VARCHAR(100) PRIMARY KEY,
    mpin VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO mpin_records (user_id, mpin, failed_attempts, is_locked) VALUES
('USER1001', '4829', 0, FALSE);
