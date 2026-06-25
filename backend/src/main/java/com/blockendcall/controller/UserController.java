package com.blockendcall.controller;

import com.blockendcall.dto.request.AcceptTermsRequest;
import com.blockendcall.dto.request.ChangePasswordRequest;
import com.blockendcall.dto.request.DeleteAccountRequest;
import com.blockendcall.dto.request.UpdatePreferencesRequest;
import com.blockendcall.dto.request.UpdateProfileRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.BadgeResponse;
import com.blockendcall.dto.response.UserPreferenceResponse;
import com.blockendcall.dto.response.UserProfileResponse;
import com.blockendcall.dto.response.UserReportResponse;
import com.blockendcall.entity.User;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.UserRepository;
import com.blockendcall.service.BadgeService;
import com.blockendcall.service.BlockedNumberService;
import com.blockendcall.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and report history")
public class UserController {

    private final BlockedNumberService blockedNumberService;
    private final UserPreferenceService userPreferenceService;
    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                blockedNumberService.getUserProfile(userDetails.getUsername())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                blockedNumberService.updateProfile(request, userDetails.getUsername())));
    }

    @PostMapping("/me/password")
    @Operation(summary = "Change my password", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        blockedNumberService.changePassword(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me/reports")
    @Operation(summary = "List numbers I have reported", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<UserReportResponse>>> myReports(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                blockedNumberService.getMyReports(userDetails.getUsername())));
    }

    @DeleteMapping("/me/reports/{reportId}")
    @Operation(summary = "Delete my report (un-report)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal UserDetails userDetails) {
        blockedNumberService.unreportNumber(reportId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me/preferences")
    @Operation(summary = "Get my preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                userPreferenceService.getPreferences(userDetails.getUsername())));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update my preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreferences(
            @RequestBody UpdatePreferencesRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                userPreferenceService.updatePreferences(request, userDetails.getUsername())));
    }

    @GetMapping("/me/badges")
    @Operation(summary = "Get my badges", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                badgeService.getBadges(userDetails.getUsername())));
    }

    @PostMapping("/me/terms")
    @Operation(summary = "Accept terms of service", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> acceptTerms(
            @RequestBody AcceptTermsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!request.isAccepted()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Você deve aceitar os termos de uso"));
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setTermsAcceptedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete my account (anonymize)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!request.isConfirmDelete()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Confirmação de exclusão obrigatória"));
        }
        blockedNumberService.deleteAccount(request.getPassword(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Conta deletada com sucesso", null));
    }
}
