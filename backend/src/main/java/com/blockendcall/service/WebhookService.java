package com.blockendcall.service;

import com.blockendcall.dto.response.WebhookResponse;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
    // This is a defense-in-depth check; the @Pattern(https) on the DTO already blocks http.
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
        webhookRepository.findById(id).ifPresent(w -> {
            w.setActive(false);
            webhookRepository.save(w);
        });
    }

    @Async("webhookExecutor")
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

        // Reject bare IPs in private ranges without a DNS lookup.
        if (isPrivateIpLiteral(host)) {
            throw new IllegalArgumentException("Webhook URL must not target private or loopback addresses");
        }

        // Resolve the hostname and check the resulting address.
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
