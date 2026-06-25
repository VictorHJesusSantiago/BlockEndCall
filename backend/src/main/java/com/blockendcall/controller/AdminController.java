package com.blockendcall.controller;

import com.blockendcall.dto.request.AdminSuspendRequest;
import com.blockendcall.dto.request.BulkActionRequest;
import com.blockendcall.dto.request.PromoteUserRequest;
import com.blockendcall.dto.response.AdminUserResponse;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.AuditLogResponse;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.repository.AuditLogRepository;
import com.blockendcall.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only management endpoints")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/users")
    @Operation(summary = "List all users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers(pageable)));
    }

    @PostMapping("/users/suspend")
    @Operation(summary = "Suspend or unsuspend a user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> suspendUser(
            @Valid @RequestBody AdminSuspendRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.suspendUser(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/users/unsuspend")
    @Operation(summary = "Unsuspend a user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> unsuspendUser(
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.unsuspendUser(body.get("userId"), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/users/promote")
    @Operation(summary = "Promote a user to a new role", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> promoteUser(
            @Valid @RequestBody PromoteUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.promoteUser(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/numbers/bulk-approve")
    @Operation(summary = "Bulk approve pending numbers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> bulkApprove(
            @Valid @RequestBody BulkActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.bulkApprove(request.getIds(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/numbers/bulk-reject")
    @Operation(summary = "Bulk reject and delete pending numbers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> bulkReject(
            @Valid @RequestBody BulkActionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminService.bulkReject(request.getIds(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/numbers/pending")
    @Operation(summary = "List pending (unconfirmed) numbers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<BlockedNumberResponse>>> getPendingNumbers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getPendingNumbers(pageable)));
    }

    @GetMapping("/audit")
    @Operation(summary = "Get audit log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLog(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> entries = auditLogRepository.findAll(pageable)
                .map(AuditLogResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }
}
