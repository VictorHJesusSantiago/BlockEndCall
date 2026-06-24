package com.blockendcall.controller;

import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.UserReportResponse;
import com.blockendcall.service.BlockedNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and report history")
public class UserController {

    private final BlockedNumberService blockedNumberService;

    @GetMapping("/me/reports")
    @Operation(summary = "List numbers I have reported", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<UserReportResponse>>> myReports(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<UserReportResponse> reports = blockedNumberService.getMyReports(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(reports));
    }
}
