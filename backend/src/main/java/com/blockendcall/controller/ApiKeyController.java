package com.blockendcall.controller;

import com.blockendcall.dto.request.CreateApiKeyRequest;
import com.blockendcall.dto.response.ApiKeyResponse;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.service.ApiKeyService;
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
@RequestMapping("/api/v1/users/me/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "Manage personal API keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    @Operation(summary = "List my API keys", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getMyKeys(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                apiKeyService.getMyKeys(userDetails.getUsername())));
    }

    @PostMapping
    @Operation(summary = "Create a new API key", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createApiKey(
            @Valid @RequestBody CreateApiKeyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApiKeyResponse response = apiKeyService.createApiKey(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke an API key", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> revokeKey(
            @PathVariable Long keyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        apiKeyService.revokeApiKey(keyId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
