package com.blockendcall.service;

import com.blockendcall.dto.response.WebhookResponse;
import com.blockendcall.entity.Webhook;
import com.blockendcall.event.NumberConfirmedEvent;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.WebhookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    // Private/link-local/loopback prefixes to block at registration time (SSRF prevention).
    // The @Pattern(https) on CreateWebhookRequest already blocks http; this is defense-in-depth.
    private static final Set<String> PRIVATE_IP_PREFIXES = Set.of(
            "10.", "127.", "0.", "169.254.",
            "192.168.",
            "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.",
            "172.25.", "172.26.", "172.27.", "172.28.", "172.29.",
            "172.30.", "172.31."
    );

    private final WebhookRepository webhookRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WebhookResponse register(String url, String secret) {
        validateWebhookUrl(url);
        Webhook webhook = webhookRepository.save(
                Webhook.builder().url(url).secret(secret).build());
        return WebhookResponse.from(webhook);
    }

    public List<WebhookResponse> listAll() {
        return webhookRepository.findAll().stream()
                .map(WebhookResponse::from)
                .toList();
    }

    public void deactivate(Long id) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
        webhook.setActive(false);
        webhookRepository.save(webhook);
    }

    /**
     * Delivers a NUMBER_CONFIRMED webhook to all active subscribers.
     *
     * Runs AFTER the business transaction commits (AFTER_COMMIT phase) so consumers
     * that call back our API always see the confirmed state in the database.
     * Runs on the webhook thread pool so delivery latency cannot block the HTTP response.
     */
    @Async("webhookExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyConfirmed(NumberConfirmedEvent event) {
        List<Webhook> active = webhookRepository.findAllByActiveTrue();
        if (active.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "event", "NUMBER_CONFIRMED",
                "phoneNumber", event.phoneNumber(),
                "category", event.category(),
                "reportCount", event.reportCount()
        );

        final String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize webhook payload for number {}", event.phoneNumber(), e);
            return;
        }

        for (Webhook webhook : active) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (webhook.getSecret() != null) {
                    // Sign the exact bytes transmitted so the receiver can verify integrity.
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
            // Never emit an empty or forgeable signature — fail the delivery instead.
            throw new IllegalStateException("Unable to compute webhook signature", e);
        }
    }

    private void validateWebhookUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid webhook URL");
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Webhook URL must use HTTPS");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Webhook URL must include a valid host");
        }

        // Reject bare private-IP literals without a DNS lookup.
        if (isPrivateIpLiteral(host)) {
            throw new IllegalArgumentException("Webhook URL must not target private or loopback addresses");
        }

        // Resolve the hostname and check the resulting address to catch hostnames
        // that map to private ranges (e.g. "internal.corp" → 10.0.0.1).
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                throw new IllegalArgumentException("Webhook URL must not target private or loopback addresses");
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Webhook URL host cannot be resolved: " + host);
        }
    }

    private boolean isPrivateIpLiteral(String host) {
        for (String prefix : PRIVATE_IP_PREFIXES) {
            if (host.startsWith(prefix)) {
                return true;
            }
        }
        return "localhost".equalsIgnoreCase(host);
    }
}
