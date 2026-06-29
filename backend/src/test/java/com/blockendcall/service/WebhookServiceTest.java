package com.blockendcall.service;

import com.blockendcall.dto.response.WebhookResponse;
import com.blockendcall.entity.Webhook;
import com.blockendcall.event.NumberConfirmedEvent;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookService — registro, entrega e segurança de webhooks")
class WebhookServiceTest {

    @Mock private WebhookRepository webhookRepository;
    @Mock private RestTemplate restTemplate;

    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(webhookRepository, restTemplate, new ObjectMapper());
    }

    // ─── register — validação de SSRF ────────────────────────────────────────

    @Test
    @DisplayName("register rejeita URL com esquema HTTP (somente HTTPS é permitido)")
    void register_httpScheme_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("http://example.com/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    @DisplayName("register rejeita localhost")
    void register_localhost_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("https://localhost/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or loopback");
    }

    @Test
    @DisplayName("register rejeita endereço IP de loopback 127.x")
    void register_loopbackIp_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("https://127.0.0.1/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or loopback");
    }

    @Test
    @DisplayName("register rejeita IP privado 192.168.x")
    void register_privateIp192_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("https://192.168.1.1/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or loopback");
    }

    @Test
    @DisplayName("register rejeita IP privado 10.x")
    void register_privateIp10_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("https://10.0.0.1/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or loopback");
    }

    @Test
    @DisplayName("register rejeita IP privado 172.16.x-172.31.x")
    void register_privateIp172_throwsIllegalArgument() {
        assertThatThrownBy(() -> webhookService.register("https://172.16.0.1/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("private or loopback");
    }

    @Test
    @DisplayName("register rejeita hostname inválido que não pode ser resolvido por DNS")
    void register_unresolvableHost_throwsIllegalArgument() {
        // .invalid é um TLD reservado que nunca resolve (RFC 2606)
        assertThatThrownBy(() -> webhookService.register("https://naoexiste.invalid/hook", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be resolved");
    }

    @Test
    @DisplayName("register salva e retorna WebhookResponse sem expor o secret")
    void register_validPublicUrl_savesAndReturnsResponse() throws Exception {
        InetAddress publicAddr = mock(InetAddress.class);
        when(publicAddr.isLoopbackAddress()).thenReturn(false);
        when(publicAddr.isLinkLocalAddress()).thenReturn(false);
        when(publicAddr.isSiteLocalAddress()).thenReturn(false);
        when(publicAddr.isAnyLocalAddress()).thenReturn(false);

        Webhook saved = Webhook.builder()
                .id(1L).url("https://hook.example.com/callback")
                .secret("meu-secret").active(true)
                .createdAt(LocalDateTime.now()).build();

        when(webhookRepository.save(any(Webhook.class))).thenReturn(saved);

        try (MockedStatic<InetAddress> mocked = mockStatic(InetAddress.class)) {
            mocked.when(() -> InetAddress.getByName("hook.example.com")).thenReturn(publicAddr);

            WebhookResponse response = webhookService.register("https://hook.example.com/callback", "meu-secret");

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.url()).isEqualTo("https://hook.example.com/callback");
            assertThat(response.active()).isTrue();
            // WebhookResponse é um record sem campo 'secret' — confirmado pela ausência de getter
        }
    }

    // ─── listAll ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("listAll retorna lista mapeada para WebhookResponse")
    void listAll_returnsResponses() {
        Webhook w1 = Webhook.builder().id(1L).url("https://a.example.com").active(true)
                .createdAt(LocalDateTime.now()).build();
        Webhook w2 = Webhook.builder().id(2L).url("https://b.example.com").active(false)
                .createdAt(LocalDateTime.now()).build();

        when(webhookRepository.findAll()).thenReturn(List.of(w1, w2));

        List<WebhookResponse> result = webhookService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(WebhookResponse::url)
                .containsExactly("https://a.example.com", "https://b.example.com");
    }

    // ─── deactivate ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deactivate seta active=false e salva o webhook")
    void deactivate_found_setsActiveFalse() {
        Webhook webhook = Webhook.builder().id(1L).active(true)
                .url("https://a.example.com").createdAt(LocalDateTime.now()).build();

        when(webhookRepository.findById(1L)).thenReturn(Optional.of(webhook));

        webhookService.deactivate(1L);

        verify(webhookRepository).save(argThat(w -> !w.isActive()));
    }

    @Test
    @DisplayName("deactivate lança ResourceNotFoundException quando ID não existe")
    void deactivate_notFound_throwsResourceNotFoundException() {
        when(webhookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> webhookService.deactivate(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── notifyConfirmed ─────────────────────────────────────────────────────

    @Test
    @DisplayName("notifyConfirmed não faz requisições quando não há webhooks ativos")
    void notifyConfirmed_noActiveWebhooks_doesNotCallRestTemplate() {
        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of());

        webhookService.notifyConfirmed(
                new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5));

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("notifyConfirmed faz POST para cada webhook ativo")
    void notifyConfirmed_activeWebhooks_postsToEachUrl() {
        Webhook w1 = Webhook.builder().id(1L).url("https://a.example.com/hook").active(true).build();
        Webhook w2 = Webhook.builder().id(2L).url("https://b.example.com/hook").active(true).build();

        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of(w1, w2));

        webhookService.notifyConfirmed(
                new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5));

        verify(restTemplate).postForEntity(eq("https://a.example.com/hook"), any(), eq(String.class));
        verify(restTemplate).postForEntity(eq("https://b.example.com/hook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("notifyConfirmed adiciona header X-BlockEndCall-Signature quando secret está configurado")
    void notifyConfirmed_withSecret_addsSignatureHeader() {
        Webhook w = Webhook.builder().id(1L)
                .url("https://a.example.com/hook").secret("meu-secret").active(true).build();

        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of(w));

        webhookService.notifyConfirmed(
                new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(), captor.capture(), eq(String.class));

        HttpEntity<String> entity = captor.getValue();
        String sig = entity.getHeaders().getFirst("X-BlockEndCall-Signature");
        assertThat(sig).isNotNull().startsWith("sha256=");
        // Garante que a assinatura usa HMAC (64 hex chars) e não é vazia
        assertThat(sig.substring("sha256=".length())).hasSize(64);
    }

    @Test
    @DisplayName("notifyConfirmed não adiciona header de assinatura quando secret é null")
    void notifyConfirmed_withoutSecret_doesNotAddSignatureHeader() {
        Webhook w = Webhook.builder().id(1L)
                .url("https://a.example.com/hook").secret(null).active(true).build();

        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of(w));

        webhookService.notifyConfirmed(
                new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(any(), captor.capture(), eq(String.class));

        assertThat(captor.getValue().getHeaders().get("X-BlockEndCall-Signature")).isNull();
    }

    @Test
    @DisplayName("notifyConfirmed continua para outros webhooks quando um falha com exceção")
    void notifyConfirmed_oneWebhookFails_continuesDelivery() {
        Webhook w1 = Webhook.builder().id(1L).url("https://a.example.com/hook").active(true).build();
        Webhook w2 = Webhook.builder().id(2L).url("https://b.example.com/hook").active(true).build();

        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of(w1, w2));
        when(restTemplate.postForEntity(eq("https://a.example.com/hook"), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatNoException().isThrownBy(() ->
                webhookService.notifyConfirmed(
                        new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5)));

        verify(restTemplate).postForEntity(eq("https://b.example.com/hook"), any(), eq(String.class));
    }

    @Test
    @DisplayName("sign produz assinaturas HMAC-SHA256 determinísticas (mesmos inputs = mesmo output)")
    void sign_deterministic_sameInputsSameOutput() throws Exception {
        // Acessa sign() indiretamente via notifyConfirmed com mesmo payload
        Webhook w = Webhook.builder().id(1L).url("https://a.example.com/hook")
                .secret("secret-fixo").active(true).build();

        when(webhookRepository.findAllByActiveTrue()).thenReturn(List.of(w, w));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        webhookService.notifyConfirmed(
                new NumberConfirmedEvent(1L, "+5511999990000", "SCAM", 5));

        verify(restTemplate, times(2)).postForEntity(any(), captor.capture(), eq(String.class));
        List<HttpEntity<String>> entities = captor.getAllValues();

        // Ambas as entregas tiveram o mesmo payload → mesma assinatura
        String sig1 = entities.get(0).getHeaders().getFirst("X-BlockEndCall-Signature");
        String sig2 = entities.get(1).getHeaders().getFirst("X-BlockEndCall-Signature");
        assertThat(sig1).isEqualTo(sig2);
    }
}
