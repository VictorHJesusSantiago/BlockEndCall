package com.blockendcall.service;

import com.blockendcall.dto.request.LogCallRequest;
import com.blockendcall.dto.response.BlockedCallLogResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.ServerBlockedCallLog;
import com.blockendcall.entity.User;
import com.blockendcall.enums.BlockedCallResult;
import com.blockendcall.enums.UserRole;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.ServerBlockedCallLogRepository;
import com.blockendcall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CallLogService — registro de chamadas bloqueadas pelo servidor")
class CallLogServiceTest {

    @Mock private ServerBlockedCallLogRepository serverBlockedCallLogRepository;
    @Mock private BlockedNumberRepository blockedNumberRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CallLogService callLogService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("Victor").email("victor@example.com")
                .role(UserRole.USER).build();
    }

    // ─── logBlockedCall ──────────────────────────────────────────────────────

    @Test
    @DisplayName("logBlockedCall salva o log com resultado REJECTED por padrão")
    void logBlockedCall_noBlockResult_defaultsToRejected() {
        LogCallRequest req = new LogCallRequest();
        req.setPhoneNumber("+5511999990000");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        callLogService.logBlockedCall(req, user.getEmail());

        ArgumentCaptor<ServerBlockedCallLog> captor = ArgumentCaptor.forClass(ServerBlockedCallLog.class);
        verify(serverBlockedCallLogRepository).save(captor.capture());

        ServerBlockedCallLog saved = captor.getValue();
        assertThat(saved.getPhoneNumber()).isEqualTo("+5511999990000");
        assertThat(saved.getBlockResult()).isEqualTo(BlockedCallResult.REJECTED);
        assertThat(saved.getMatchedNumber()).isNull();
    }

    @Test
    @DisplayName("logBlockedCall salva resultado SILENCED quando informado")
    void logBlockedCall_withBlockResult_usesProvidedResult() {
        LogCallRequest req = new LogCallRequest();
        req.setPhoneNumber("+5511999990000");
        req.setBlockResult(BlockedCallResult.SILENCED);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        callLogService.logBlockedCall(req, user.getEmail());

        ArgumentCaptor<ServerBlockedCallLog> captor = ArgumentCaptor.forClass(ServerBlockedCallLog.class);
        verify(serverBlockedCallLogRepository).save(captor.capture());
        assertThat(captor.getValue().getBlockResult()).isEqualTo(BlockedCallResult.SILENCED);
    }

    @Test
    @DisplayName("logBlockedCall vincula o número correspondente quando matchedNumberId é informado")
    void logBlockedCall_withMatchedNumberId_linksBlockedNumber() {
        BlockedNumber blocked = BlockedNumber.builder().id(42L).phoneNumber("+5511999990000").build();

        LogCallRequest req = new LogCallRequest();
        req.setPhoneNumber("+5511999990000");
        req.setMatchedNumberId(42L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findById(42L)).thenReturn(Optional.of(blocked));

        callLogService.logBlockedCall(req, user.getEmail());

        ArgumentCaptor<ServerBlockedCallLog> captor = ArgumentCaptor.forClass(ServerBlockedCallLog.class);
        verify(serverBlockedCallLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMatchedNumber()).isEqualTo(blocked);
    }

    @Test
    @DisplayName("logBlockedCall ignora matchedNumberId inexistente sem lançar exceção")
    void logBlockedCall_matchedNumberNotFound_savesWithNullMatchedNumber() {
        LogCallRequest req = new LogCallRequest();
        req.setPhoneNumber("+5511999990000");
        req.setMatchedNumberId(99L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findById(99L)).thenReturn(Optional.empty());

        callLogService.logBlockedCall(req, user.getEmail());

        ArgumentCaptor<ServerBlockedCallLog> captor = ArgumentCaptor.forClass(ServerBlockedCallLog.class);
        verify(serverBlockedCallLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMatchedNumber()).isNull();
    }

    @Test
    @DisplayName("logBlockedCall lança UsernameNotFoundException quando usuário não existe")
    void logBlockedCall_unknownUser_throwsUsernameNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        LogCallRequest reqUnknown = new LogCallRequest();
        reqUnknown.setPhoneNumber("+5511999990000");
        assertThatThrownBy(() -> callLogService.logBlockedCall(reqUnknown, "desconhecido@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ─── getMyCallLog ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyCallLog retorna página de logs do usuário autenticado")
    void getMyCallLog_returnsPageForUser() {
        ServerBlockedCallLog log = ServerBlockedCallLog.builder()
                .id(1L).user(user).phoneNumber("+5511999990000")
                .blockResult(BlockedCallResult.REJECTED)
                .blockedAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(serverBlockedCallLogRepository.findByUserId(user.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(log)));

        var result = callLogService.getMyCallLog(user.getEmail(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPhoneNumber()).isEqualTo("+5511999990000");
    }

    // ─── getMyBlockedCallCount ───────────────────────────────────────────────

    @Test
    @DisplayName("getMyBlockedCallCount retorna a contagem total do repositório")
    void getMyBlockedCallCount_returnsCount() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(serverBlockedCallLogRepository.countByUserId(user.getId())).thenReturn(42L);

        long count = callLogService.getMyBlockedCallCount(user.getEmail());

        assertThat(count).isEqualTo(42L);
    }
}
