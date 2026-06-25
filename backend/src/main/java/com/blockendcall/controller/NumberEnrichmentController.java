package com.blockendcall.controller;

import com.blockendcall.dto.request.MeTooRequest;
import com.blockendcall.dto.request.SubmitReportedNameRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.NumberReportedNameResponse;
import com.blockendcall.dto.response.NumberTimelineResponse;
import com.blockendcall.service.BlockedNumberService;
import com.blockendcall.service.NumberEnrichmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/numbers")
@RequiredArgsConstructor
@Tag(name = "Number Enrichment", description = "Reported names, timeline, and me-too confirmations")
public class NumberEnrichmentController {

    private final NumberEnrichmentService numberEnrichmentService;
    private final BlockedNumberService blockedNumberService;

    @GetMapping("/{id}/reported-names")
    @Operation(summary = "Get reported caller names for a number", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<NumberReportedNameResponse>>> getReportedNames(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(numberEnrichmentService.getReportedNames(id)));
    }

    @PostMapping("/{id}/reported-names")
    @Operation(summary = "Submit a reported caller name", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> submitReportedName(
            @PathVariable Long id,
            @Valid @RequestBody SubmitReportedNameRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        numberEnrichmentService.submitReportedName(id, request.getReportedName(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get activity timeline for a number", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<NumberTimelineResponse>>> getTimeline(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(numberEnrichmentService.getTimeline(id)));
    }

    @PostMapping("/{numberId}/confirm")
    @Operation(summary = "Me Too — confirm you also received spam from this number",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> meToo(
            @PathVariable Long numberId,
            @RequestBody(required = false) MeTooRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        blockedNumberService.confirmMeToo(numberId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Confirmação registrada", null));
    }
}
