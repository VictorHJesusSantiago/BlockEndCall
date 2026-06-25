package com.blockendcall.controller;

import com.blockendcall.dto.request.AddPublicWhitelistRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.PublicWhitelistResponse;
import com.blockendcall.service.PublicWhitelistService;
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
@RequestMapping("/api/v1/public-whitelist")
@RequiredArgsConstructor
@Tag(name = "Public Whitelist", description = "Community-verified legitimate numbers")
public class PublicWhitelistController {

    private final PublicWhitelistService publicWhitelistService;

    @GetMapping
    @Operation(summary = "List verified public whitelist entries (public)")
    public ResponseEntity<ApiResponse<Page<PublicWhitelistResponse>>> listVerified(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(publicWhitelistService.listVerified(pageable)));
    }

    @GetMapping("/check/{phone}")
    @Operation(summary = "Check if a phone number is in the public whitelist (public)")
    public ResponseEntity<ApiResponse<PublicWhitelistResponse>> findByPhone(
            @PathVariable String phone) {
        return publicWhitelistService.findByPhone(phone)
                .map(r -> ResponseEntity.ok(ApiResponse.ok(r)))
                .orElse(ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a number to public whitelist (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PublicWhitelistResponse>> add(
            @Valid @RequestBody AddPublicWhitelistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PublicWhitelistResponse response = publicWhitelistService.add(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a public whitelist entry as verified (admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> verify(@PathVariable Long id) {
        publicWhitelistService.verify(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
