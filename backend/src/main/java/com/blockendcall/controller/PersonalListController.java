package com.blockendcall.controller;

import com.blockendcall.dto.request.PersonalListRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.PersonalListResponse;
import com.blockendcall.service.PersonalListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Personal Lists", description = "Personal whitelist and blacklist management")
public class PersonalListController {

    private final PersonalListService personalListService;

    // ── Whitelist ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/users/me/personal-whitelist")
    @Operation(summary = "Get my personal whitelist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<PersonalListResponse>>> getMyWhitelist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                personalListService.getMyWhitelist(userDetails.getUsername())));
    }

    @PostMapping("/api/v1/users/me/personal-whitelist")
    @Operation(summary = "Add number to personal whitelist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PersonalListResponse>> addToWhitelist(
            @Valid @RequestBody PersonalListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                personalListService.addToWhitelist(request, userDetails.getUsername())));
    }

    @DeleteMapping("/api/v1/users/me/personal-whitelist/{phone}")
    @Operation(summary = "Remove number from personal whitelist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> removeFromWhitelist(
            @PathVariable String phone,
            @AuthenticationPrincipal UserDetails userDetails) {
        personalListService.removeFromWhitelist(phone, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Blacklist ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/users/me/personal-blacklist")
    @Operation(summary = "Get my personal blacklist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<PersonalListResponse>>> getMyBlacklist(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                personalListService.getMyBlacklist(userDetails.getUsername())));
    }

    @PostMapping("/api/v1/users/me/personal-blacklist")
    @Operation(summary = "Add number to personal blacklist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PersonalListResponse>> addToBlacklist(
            @Valid @RequestBody PersonalListRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                personalListService.addToBlacklist(request, userDetails.getUsername())));
    }

    @DeleteMapping("/api/v1/users/me/personal-blacklist/{phone}")
    @Operation(summary = "Remove number from personal blacklist", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> removeFromBlacklist(
            @PathVariable String phone,
            @AuthenticationPrincipal UserDetails userDetails) {
        personalListService.removeFromBlacklist(phone, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
