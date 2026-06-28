package com.blockendcall.controller;

import com.blockendcall.dto.request.MeTooRequest;
import com.blockendcall.dto.request.SubmitReportedNameRequest;
import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.NumberReportedNameResponse;
import com.blockendcall.dto.response.NumberTimelineResponse;
import com.blockendcall.service.BlockedNumberService;
import com.blockendcall.service.NumberEnrichmentService;
import com.blockendcall.service.OperatorLookupService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/numbers")
@RequiredArgsConstructor
@Tag(name = "Number Enrichment", description = "Reported names, timeline, and me-too confirmations")
public class NumberEnrichmentController {

    private final NumberEnrichmentService numberEnrichmentService;
    private final BlockedNumberService blockedNumberService;
    private final OperatorLookupService operatorLookupService;

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

    @GetMapping("/ddd/{ddd}")
    @Operation(summary = "Get city/region for a Brazilian DDD area code (no auth)")
    public ResponseEntity<ApiResponse<String>> lookupDdd(@PathVariable String ddd) {
        return ResponseEntity.ok(ApiResponse.ok(operatorLookupService.getAllDdds()
                .getOrDefault(ddd, "Desconhecido")));
    }

    @GetMapping("/ddd")
    @Operation(summary = "Get all Brazilian DDD area codes with city names (no auth)")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllDdds() {
        return ResponseEntity.ok(ApiResponse.ok(operatorLookupService.getAllDdds()));
    }
}
