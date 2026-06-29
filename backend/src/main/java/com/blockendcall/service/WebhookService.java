package com.blockendcall.service;

import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.Webhook;
import com.blockendcall.repository.WebhookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final WebhookRepository webhookRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Webhook register(String url, String secret) {
        return webhookRepository.save(Webhook.builder().url(url).secret(secret).build());
    }

    public List<Webhook> listAll() {
        return webhookRepository.findAll();
    }

    public void deactivate(Long id) {
        webhookRepository.findById(id).ifPresent(w -> {
            w.setActive(false);
            webhookRepository.save(w);
        });
    }

    public void notifyConfirmed(BlockedNumber number) {
        List<Webhook> active = webhookRepository.findAllByActiveTrue();
        if (active.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
            "event", "NUMBER_CONFIRMED",
            "phoneNumber", number.getPhoneNumber(),
            "category", number.getCategory() != null ? number.getCategory().name() : "UNKNOWN",
            "reportCount", number.getReportCount()
        );

        final String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize webhook payload for number {}", number.getPhoneNumber(), e);
            return;
        }

        for (Webhook webhook : active) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (webhook.getSecret() != null) {
                    // Sign the exact bytes we transmit so the receiver can verify integrity.
                    headers.set("X-BlockEndCall-Signature", "sha256=" + sign(webhook.getSecret(), body));
                }
                HttpEntity<String> entity = new HttpEntity<>(body, headers);
                restTemplate.postForEntity(webhook.getUrl(), entity, String.class);
            } catch (Exception e) {
                log.warn("Webhook delivery failed for {}: {}", webhook.getUrl(), e.getMessage());
            }
        }
    }

    private String sign(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmac);
        } catch (Exception e) {
            // Never ship an empty/forgeable signature: fail the delivery instead.
            throw new IllegalStateException("Unable to compute webhook signature", e);
        }
    }
}
