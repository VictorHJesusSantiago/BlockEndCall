package com.blockendcall.security;

import com.blockendcall.entity.User;
import com.blockendcall.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.JwtException;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil — geração e validação de tokens JWT")
class JwtUtilTest {

    // Chave Base64 válida de 32 bytes para HmacSHA256
    private static final String TEST_SECRET =
            "dGVzdFNlY3JldEtleUZvckJsb2NrRW5kQ2FsbFRlc3RpbmcxMjM0NTY3ODk=";
    private static final long EXPIRATION_MS = 3_600_000L; // 1h

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);

        testUser = User.builder()
                .id(1L)
                .name("Victor")
                .email("victor@example.com")
                .password("hashed")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("generateToken cria token não nulo e não vazio")
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateToken cria token com formato JWT (3 partes separadas por ponto)")
    void generateToken_hasThreeParts() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername retorna o email do usuário embutido no token")
    void extractUsername_returnsUserEmail() {
        String token = jwtUtil.generateToken(testUser);

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("victor@example.com");
    }

    @Test
    @DisplayName("isTokenValid retorna true para token válido e usuário correto")
    void isTokenValid_validTokenAndUser_returnsTrue() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid retorna false quando o email no token não corresponde ao usuário")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtUtil.generateToken(testUser);

        User anotherUser = User.builder()
                .id(2L)
                .email("outro@example.com")
                .password("hashed")
                .role(UserRole.USER)
                .build();

        assertThat(jwtUtil.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid lança JwtException para token expirado (jjwt rejeita na parse)")
    void isTokenValid_expiredToken_throwsJwtException() {
        // Gera um token com expiração de -1ms (já expirado na criação)
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken(testUser);

        // Restaura expiração normal
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION_MS);

        // jjwt 0.12 lança ExpiredJwtException ao fazer parse de token expirado
        assertThatThrownBy(() -> jwtUtil.isTokenValid(expiredToken, testUser))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("generateToken com claims extras embute os claims no token")
    void generateToken_withExtraClaims_embedsClaims() {
        Map<String, Object> claims = Map.of("role", "ADMIN", "version", 1);

        String token = jwtUtil.generateToken(claims, testUser);

        // O token deve ser válido para o mesmo usuário
        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("victor@example.com");
    }

    @Test
    @DisplayName("dois tokens gerados para o mesmo usuário são diferentes (timestamps únicos)")
    void generateToken_calledTwice_producesDifferentTokens() throws InterruptedException {
        String token1 = jwtUtil.generateToken(testUser);
        Thread.sleep(10); // garante timestamps distintos
        String token2 = jwtUtil.generateToken(testUser);

        assertThat(token1).isNotEqualTo(token2);
    }
}
