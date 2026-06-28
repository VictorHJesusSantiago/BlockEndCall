package com.blockendcall.service;

import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.Webhook;
import com.blockendcall.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final RestTemplate restTemplate;

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
        Map<String, Object> payload = Map.of(
            "event", "NUMBER_CONFIRMED",
            "phoneNumber", number.getPhoneNumber(),
            "category", number.getCategory() != null ? number.getCategory().name() : "UNKNOWN",
            "reportCount", number.getReportCount()
        );
        for (Webhook webhook : active) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                if (webhook.getSecret() != null) {
                    headers.set("X-BlockEndCall-Signature", sign(webhook.getSecret(), number.getPhoneNumber()));
                }
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                restTemplate.postForEntity(webhook.getUrl(), entity, String.class);
            } catch (Exception e) {
                log.warn("Webhook delivery failed for {}: {}", webhook.getUrl(), e.getMessage());
            }
        }
    }

    private String sign(String secret, String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((secret + data).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }
}
