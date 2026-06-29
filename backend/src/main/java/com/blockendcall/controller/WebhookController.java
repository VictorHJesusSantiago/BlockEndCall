package com.blockendcall.controller;

import com.blockendcall.dto.request.CreateWebhookRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.WebhookResponse;
import com.blockendcall.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<ApiResponse<WebhookResponse>> register(
            @Valid @RequestBody CreateWebhookRequest request) {
        WebhookResponse response = webhookService.register(request.url(), request.secret());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Webhook registered", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WebhookResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(webhookService.listAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        webhookService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Webhook deactivated", null));
    }
}
