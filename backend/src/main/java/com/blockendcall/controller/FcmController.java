package com.blockendcall.controller;

import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/me/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM", description = "Firebase Cloud Messaging token registration")
public class FcmController {

    private final FcmService fcmService;

    @PostMapping
    @Operation(summary = "Register FCM push notification token",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        fcmService.registerToken(
                body.get("token"),
                body.get("deviceId"),
                userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
