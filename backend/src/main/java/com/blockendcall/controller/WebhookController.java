package com.blockendcall.controller;

import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.entity.Webhook;
import com.blockendcall.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    public ResponseEntity<ApiResponse<Webhook>> register(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok(webhookService.register(body.get("url"), body.get("secret"))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Webhook>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(webhookService.listAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        webhookService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok("Webhook deactivated", null));
    }
}
