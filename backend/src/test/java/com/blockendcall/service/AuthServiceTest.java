package com.blockendcall.service;

import com.blockendcall.dto.request.LoginRequest;
import com.blockendcall.dto.request.RegisterRequest;
import com.blockendcall.dto.response.AuthResponse;
import com.blockendcall.entity.User;
import com.blockendcall.enums.UserRole;
import com.blockendcall.repository.UserRepository;
import com.blockendcall.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — autenticação e registro de usuários")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    // ─── register ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("register cria usuário e retorna token quando email é inédito")
    void register_newEmail_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Victor");
        request.setEmail("victor@example.com");
        request.setPassword("Senha@123");
        request.setPhone("+5511999990000");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .id(1L).name(u.getName()).email(u.getEmail())
                    .password(u.getPassword()).role(UserRole.USER)
                    .build();
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getEmail()).isEqualTo("victor@example.com");
        assertThat(response.getRole()).isEqualTo("USER");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register lança IllegalArgumentException quando email já está cadastrado")
    void register_duplicateEmail_throwsIllegalArgument() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Victor");
        request.setEmail("victor@example.com");
        request.setPassword("Senha@123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register codifica a senha antes de salvar")
    void register_encodesPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Victor");
        request.setEmail("victor@example.com");
        request.setPassword("plaintext");

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$bcrypt$hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder().id(1L).name(u.getName()).email(u.getEmail())
                    .password(u.getPassword()).role(UserRole.USER).build();
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn("tok");

        authService.register(request);

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(u -> u.getPassword().equals("$bcrypt$hash")));
    }

    // ─── login ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login retorna AuthResponse quando credenciais estão corretas")
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("victor@example.com");
        request.setPassword("Senha@123");

        User user = User.builder()
                .id(1L).name("Victor").email("victor@example.com")
                .password("hashed").role(UserRole.USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-login-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-login-token");
        assertThat(response.getEmail()).isEqualTo("victor@example.com");
        verify(authenticationManager).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken
                        && auth.getPrincipal().equals("victor@example.com")));
    }

    @Test
    @DisplayName("login propaga BadCredentialsException do AuthenticationManager")
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("victor@example.com");
        request.setPassword("errada");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ─── stubs de funcionalidades não implementadas ─────────────────────────

    @Test
    @DisplayName("verifyEmail lança UnsupportedOperationException (não implementado)")
    void verifyEmail_notImplemented_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> authService.verifyEmail("algum-token"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not yet available");
    }

    @Test
    @DisplayName("sendPasswordResetEmail lança UnsupportedOperationException (não implementado)")
    void sendPasswordResetEmail_notImplemented_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> authService.sendPasswordResetEmail("victor@example.com"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not yet available");
    }

    @Test
    @DisplayName("resetPassword lança UnsupportedOperationException (não implementado)")
    void resetPassword_notImplemented_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> authService.resetPassword("tok", "Nova@Senha123"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not yet available");
    }
}
