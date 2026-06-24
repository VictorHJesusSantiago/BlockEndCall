package com.blockendcall.controller;

import com.blockendcall.dto.request.ReportNumberRequest;
import com.blockendcall.dto.request.WhitelistRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.NumberCheckResponse;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.service.BlockedNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/numbers")
@RequiredArgsConstructor
@Tag(name = "Blocked Numbers", description = "Report and query spam numbers")
public class BlockedNumberController {

    private final BlockedNumberService blockedNumberService;

    @GetMapping("/check/{phoneNumber}")
    @Operation(summary = "Check if a phone number is spam — returns score and risk level (no auth)")
    public ResponseEntity<ApiResponse<NumberCheckResponse>> checkNumber(
            @PathVariable String phoneNumber) {

        NumberCheckResponse result = blockedNumberService.checkNumber(phoneNumber);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping
    @Operation(summary = "List confirmed blocked numbers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<BlockedNumberResponse>>> listNumbers(
            @PageableDefault(size = 20, sort = "reportCount") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(blockedNumberService.listConfirmedNumbers(pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search confirmed numbers by partial phone match", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<BlockedNumberResponse>>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(blockedNumberService.searchNumbers(q, pageable)));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter confirmed numbers by spam category", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<BlockedNumberResponse>>> listByCategory(
            @PathVariable SpamCategory category,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.ok(blockedNumberService.listByCategory(category, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a blocked number by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<BlockedNumberResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(blockedNumberService.getById(id)));
    }

    @PostMapping("/report")
    @Operation(summary = "Report a spam/scam number", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<BlockedNumberResponse>> reportNumber(
            @Valid @RequestBody ReportNumberRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        BlockedNumberResponse response = blockedNumberService.reportNumber(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Número reportado", response));
    }

    @PostMapping("/{id}/false-positive")
    @Operation(summary = "Report a number as a false positive (not spam)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> reportFalsePositive(
            @PathVariable Long id,
            @Valid @RequestBody WhitelistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        blockedNumberService.reportFalsePositive(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok("Falso positivo registrado", null));
    }

    @PatchMapping("/{id}/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Whitelist a number — removes it from spam list (admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> whitelist(@PathVariable Long id) {
        blockedNumberService.adminWhitelist(id);
        return ResponseEntity.ok(ApiResponse.ok("Número adicionado à whitelist", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a blocked number (admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteNumber(@PathVariable Long id) {
        blockedNumberService.deleteNumber(id);
        return ResponseEntity.ok(ApiResponse.ok("Número removido", null));
    }
}
