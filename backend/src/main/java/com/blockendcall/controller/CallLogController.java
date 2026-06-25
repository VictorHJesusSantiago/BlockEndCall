package com.blockendcall.controller;

import com.blockendcall.dto.request.LogCallRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.BlockedCallLogResponse;
import com.blockendcall.service.CallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/call-log")
@RequiredArgsConstructor
@Tag(name = "Call Log", description = "Server-side blocked call log")
public class CallLogController {

    private final CallLogService callLogService;

    @PostMapping
    @Operation(summary = "Log a blocked call", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logBlockedCall(
            @Valid @RequestBody LogCallRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        callLogService.logBlockedCall(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    @Operation(summary = "Get my blocked call log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<BlockedCallLogResponse>>> getMyCallLog(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                callLogService.getMyCallLog(userDetails.getUsername(), pageable)));
    }

    @GetMapping("/count")
    @Operation(summary = "Get total blocked call count", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> getCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                callLogService.getMyBlockedCallCount(userDetails.getUsername())));
    }
}
