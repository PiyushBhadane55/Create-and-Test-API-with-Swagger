package com.example.demo.controller;

import com.example.demo.entity.MpinRecordEntity;
import com.example.demo.model.*;
import com.example.demo.repository.MpinRepository;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/mpin")
@Tag(name = "MPIN API", description = "Endpoints for setting up, verifying, changing, and resetting Mobile Banking PINs (MPIN) with PostgreSQL persistence")
public class MpinController {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final MpinRepository mpinRepository;

    public MpinController(MpinRepository mpinRepository) {
        this.mpinRepository = mpinRepository;
    }

    @Operation(summary = "Set up initial MPIN", description = "Sets up a 4 to 6 digit MPIN for a user. Rejects weak PINs (sequential/repeating).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MPIN set up successfully",
                    content = @Content(schema = @Schema(implementation = MpinResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payload, non-matching confirmation, or weak MPIN"),
            @ApiResponse(responseCode = "409", description = "MPIN already exists for this user")
    })
    @PostMapping("/setup")
    public ResponseEntity<MpinResponse> setupMpin(@Valid @RequestBody MpinSetupRequest request) {
        if (!request.getMpin().equals(request.getConfirmMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "CONFIRMATION_MISMATCH", "MPIN and Confirm MPIN do not match")
            );
        }

        if (isWeakMpin(request.getMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "WEAK_MPIN", "Weak MPIN: Sequential (e.g. 1234) or repeating digits (e.g. 1111) are not allowed")
            );
        }

        if (mpinRepository.existsById(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new MpinResponse(false, "MPIN_ALREADY_EXISTS", "MPIN is already configured for this user. Use /change or /reset endpoints.")
            );
        }

        MpinRecordEntity newRecord = new MpinRecordEntity(request.getUserId(), request.getMpin());
        mpinRepository.save(newRecord);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new MpinResponse(true, "MPIN_SETUP_SUCCESS", "MPIN set up successfully", MAX_FAILED_ATTEMPTS, false)
        );
    }

    @Operation(summary = "Verify MPIN", description = "Verifies user MPIN. Locks account after 3 consecutive failed attempts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MPIN verified successfully"),
            @ApiResponse(responseCode = "401", description = "Incorrect MPIN (returns remaining attempts)"),
            @ApiResponse(responseCode = "404", description = "User not found or MPIN not set up"),
            @ApiResponse(responseCode = "423", description = "Account locked due to 3 consecutive failed attempts")
    })
    @PostMapping("/verify")
    public ResponseEntity<MpinResponse> verifyMpin(@Valid @RequestBody MpinVerifyRequest request) {
        Optional<MpinRecordEntity> recordOpt = mpinRepository.findById(request.getUserId());
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MpinResponse(false, "USER_NOT_FOUND", "No MPIN record found for user: " + request.getUserId())
            );
        }

        MpinRecordEntity record = recordOpt.get();
        if (record.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(
                    new MpinResponse(false, "ACCOUNT_LOCKED", "Account is locked due to 3 consecutive failed attempts. Reset required.", 0, true)
            );
        }

        if (record.getMpin().equals(request.getMpin())) {
            record.setFailedAttempts(0);
            mpinRepository.save(record);
            return ResponseEntity.ok(
                    new MpinResponse(true, "MPIN_VERIFIED_SUCCESS", "MPIN verified successfully", MAX_FAILED_ATTEMPTS, false)
            );
        } else {
            int failed = record.getFailedAttempts() + 1;
            record.setFailedAttempts(failed);
            int remaining = MAX_FAILED_ATTEMPTS - failed;

            if (remaining <= 0) {
                record.setLocked(true);
                mpinRepository.save(record);
                return ResponseEntity.status(HttpStatus.LOCKED).body(
                        new MpinResponse(false, "ACCOUNT_LOCKED", "Incorrect MPIN. Maximum failed attempts reached. Account is now locked.", 0, true)
                );
            } else {
                mpinRepository.save(record);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new MpinResponse(false, "INVALID_MPIN", "Incorrect MPIN. Remaining attempts: " + remaining, remaining, false)
                );
            }
        }
    }

    @Operation(summary = "Change MPIN", description = "Changes MPIN after verifying old MPIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MPIN changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or weak new MPIN"),
            @ApiResponse(responseCode = "401", description = "Old MPIN verification failed"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "423", description = "Account is locked")
    })
    @PostMapping("/change")
    public ResponseEntity<MpinResponse> changeMpin(@Valid @RequestBody MpinChangeRequest request) {
        Optional<MpinRecordEntity> recordOpt = mpinRepository.findById(request.getUserId());
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MpinResponse(false, "USER_NOT_FOUND", "User not found")
            );
        }

        MpinRecordEntity record = recordOpt.get();
        if (record.isLocked()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(
                    new MpinResponse(false, "ACCOUNT_LOCKED", "Account is locked. Use /reset endpoint.", 0, true)
            );
        }

        if (!record.getMpin().equals(request.getOldMpin())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new MpinResponse(false, "OLD_MPIN_MISMATCH", "Current MPIN is incorrect")
            );
        }

        if (!request.getNewMpin().equals(request.getConfirmNewMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "CONFIRMATION_MISMATCH", "New MPIN and Confirm MPIN do not match")
            );
        }

        if (isWeakMpin(request.getNewMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "WEAK_MPIN", "Weak MPIN: Sequential or repeating digits not allowed")
            );
        }

        record.setMpin(request.getNewMpin());
        record.setFailedAttempts(0);
        mpinRepository.save(record);

        return ResponseEntity.ok(
                new MpinResponse(true, "MPIN_CHANGED_SUCCESS", "MPIN successfully updated", MAX_FAILED_ATTEMPTS, false)
        );
    }

    @Operation(summary = "Reset MPIN", description = "Unlocks a locked account and resets MPIN using an OTP reset token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MPIN reset and account unlocked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or payload"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/reset")
    public ResponseEntity<MpinResponse> resetMpin(@Valid @RequestBody MpinResetRequest request) {
        Optional<MpinRecordEntity> recordOpt = mpinRepository.findById(request.getUserId());
        if (recordOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MpinResponse(false, "USER_NOT_FOUND", "User not found")
            );
        }

        if (request.getResetToken() == null || request.getResetToken().isBlank()) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "INVALID_TOKEN", "Valid reset token / OTP is required")
            );
        }

        if (!request.getNewMpin().equals(request.getConfirmNewMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "CONFIRMATION_MISMATCH", "New MPIN and Confirm MPIN do not match")
            );
        }

        if (isWeakMpin(request.getNewMpin())) {
            return ResponseEntity.badRequest().body(
                    new MpinResponse(false, "WEAK_MPIN", "Weak MPIN: Sequential or repeating digits not allowed")
            );
        }

        MpinRecordEntity record = recordOpt.get();
        record.setMpin(request.getNewMpin());
        record.setFailedAttempts(0);
        record.setLocked(false);
        mpinRepository.save(record);

        return ResponseEntity.ok(
                new MpinResponse(true, "MPIN_RESET_SUCCESS", "Account unlocked and MPIN reset successfully", MAX_FAILED_ATTEMPTS, false)
        );
    }

    @Operation(summary = "Get MPIN status", description = "Fetches MPIN configuration and lock status for a user.")
    @GetMapping("/status/{userId}")
    public ResponseEntity<MpinStatusResponse> getMpinStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {
        Optional<MpinRecordEntity> recordOpt = mpinRepository.findById(userId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.ok(new MpinStatusResponse(userId, false, false, 0, MAX_FAILED_ATTEMPTS));
        } else {
            MpinRecordEntity record = recordOpt.get();
            return ResponseEntity.ok(new MpinStatusResponse(
                    userId,
                    true,
                    record.isLocked(),
                    record.getFailedAttempts(),
                    MAX_FAILED_ATTEMPTS
            ));
        }
    }

    private boolean isWeakMpin(String mpin) {
        if (mpin == null || mpin.length() < 4) {
            return true;
        }

        boolean allSame = true;
        for (int i = 1; i < mpin.length(); i++) {
            if (mpin.charAt(i) != mpin.charAt(0)) {
                allSame = false;
                break;
            }
        }
        if (allSame) return true;

        boolean ascendingSeq = true;
        for (int i = 1; i < mpin.length(); i++) {
            if (mpin.charAt(i) - mpin.charAt(i - 1) != 1) {
                ascendingSeq = false;
                break;
            }
        }
        if (ascendingSeq) return true;

        boolean descendingSeq = true;
        for (int i = 1; i < mpin.length(); i++) {
            if (mpin.charAt(i - 1) - mpin.charAt(i) != 1) {
                descendingSeq = false;
                break;
            }
        }
        return descendingSeq;
    }
}
