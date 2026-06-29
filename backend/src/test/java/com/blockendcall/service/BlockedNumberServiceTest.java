package com.blockendcall.service;

import com.blockendcall.dto.request.ReportNumberRequest;
import com.blockendcall.dto.request.WhitelistRequest;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.NumberCheckResponse;
import com.blockendcall.entity.*;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.enums.UserRole;
import com.blockendcall.event.NumberConfirmedEvent;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockedNumberService — relatório e consulta de números bloqueados")
class BlockedNumberServiceTest {

    @Mock private BlockedNumberRepository blockedNumberRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private FalsePositiveRepository falsePositiveRepository;
    @Mock private PersonalWhitelistRepository personalWhitelistRepository;
    @Mock private PersonalBlacklistRepository personalBlacklistRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private BlockedNumberService service;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "reportThreshold", 5);

        user = User.builder()
                .id(1L).name("Victor").email("victor@example.com")
                .role(UserRole.USER).build();
    }

    // ─── checkNumber ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkNumber retorna resposta segura quando número não existe na base")
    void checkNumber_notFound_returnsSafeResponse() {
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        NumberCheckResponse result = service.checkNumber("+5511999990000");

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getPhoneNumber()).isEqualTo("+5511999990000");
    }

    @Test
    @DisplayName("checkNumber retorna resposta segura quando número está na whitelist global")
    void checkNumber_whitelisted_returnsSafeResponse() {
        BlockedNumber whitelisted = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511999990000")
                .whitelisted(true).confirmed(true).reportCount(10)
                .build();

        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(whitelisted));

        NumberCheckResponse result = service.checkNumber("+5511999990000");

        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("checkNumber retorna blocked=true quando número está confirmado")
    void checkNumber_confirmedNumber_returnsBlockedTrue() {
        BlockedNumber confirmed = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511999990000")
                .confirmed(true).whitelisted(false)
                .category(SpamCategory.SCAM).reportCount(5)
                .build();

        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(confirmed));

        NumberCheckResponse result = service.checkNumber("+5511999990000");

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.isConfirmed()).isTrue();
    }

    @Test
    @DisplayName("checkNumber retorna blocked=true mesmo para número não confirmado (ainda pendente)")
    void checkNumber_pendingNumber_returnsBlockedTrue() {
        BlockedNumber pending = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511999990000")
                .confirmed(false).whitelisted(false)
                .reportCount(3).category(SpamCategory.TELEMARKETING)
                .build();

        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(pending));

        NumberCheckResponse result = service.checkNumber("+5511999990000");

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.isConfirmed()).isFalse();
    }

    // ─── reportNumber ────────────────────────────────────────────────────────

    @Test
    @DisplayName("reportNumber cria novo BlockedNumber quando número ainda não existe")
    void reportNumber_newNumber_createsAndSaves() {
        ReportNumberRequest req = new ReportNumberRequest();
        req.setPhoneNumber("+5511111111111");
        req.setCategory(SpamCategory.TELEMARKETING);
        req.setDescription("Ligação de telemarketing");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findByPhoneNumber(req.getPhoneNumber())).thenReturn(Optional.empty());
        when(blockedNumberRepository.save(any(BlockedNumber.class))).thenAnswer(inv -> {
            BlockedNumber bn = inv.getArgument(0);
            return BlockedNumber.builder()
                    .id(10L).phoneNumber(bn.getPhoneNumber())
                    .category(bn.getCategory()).reportCount(bn.getReportCount())
                    .confirmed(bn.isConfirmed()).build();
        });

        BlockedNumberResponse response = service.reportNumber(req, user.getEmail());

        assertThat(response).isNotNull();
        verify(blockedNumberRepository).save(any(BlockedNumber.class));
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    @DisplayName("reportNumber auto-confirma número ao atingir o threshold de reports")
    void reportNumber_thresholdReached_confirmsNumber() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511999990000")
                .reportCount(4).confirmed(false)
                .category(SpamCategory.SCAM).build();

        ReportNumberRequest req = new ReportNumberRequest();
        req.setPhoneNumber("+5511999990000");
        req.setCategory(SpamCategory.SCAM);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(any(), any())).thenReturn(false);
        when(blockedNumberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BlockedNumberResponse response = service.reportNumber(req, user.getEmail());

        assertThat(response.isConfirmed()).isTrue();
        assertThat(response.getReportCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("reportNumber lança DuplicateReportException quando usuário já reportou o número")
    void reportNumber_duplicateReport_throwsDuplicateReportException() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(5L).phoneNumber("+5511111111111").reportCount(1)
                .build();

        ReportNumberRequest req = new ReportNumberRequest();
        req.setPhoneNumber("+5511111111111");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findByPhoneNumber(req.getPhoneNumber())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(user.getId(), existing.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.reportNumber(req, user.getEmail()))
                .isInstanceOf(DuplicateReportException.class);

        verify(blockedNumberRepository, never()).save(any());
    }

    @Test
    @DisplayName("reportNumber publica NumberConfirmedEvent quando threshold é atingido pela primeira vez")
    void reportNumber_thresholdCrossed_publishesConfirmedEvent() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(5L).phoneNumber("+5511111111111")
                .confirmed(false).reportCount(4)
                .category(SpamCategory.SCAM).build();

        ReportNumberRequest req = new ReportNumberRequest();
        req.setPhoneNumber("+5511111111111");
        req.setCategory(SpamCategory.SCAM);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findByPhoneNumber(req.getPhoneNumber())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(any(), any())).thenReturn(false);
        when(blockedNumberRepository.save(any(BlockedNumber.class))).thenAnswer(inv -> inv.getArgument(0));

        service.reportNumber(req, user.getEmail());

        ArgumentCaptor<NumberConfirmedEvent> captor = ArgumentCaptor.forClass(NumberConfirmedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        NumberConfirmedEvent event = captor.getValue();
        assertThat(event.phoneNumber()).isEqualTo("+5511111111111");
        assertThat(event.category()).isEqualTo("SCAM");
        assertThat(event.reportCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("reportNumber NÃO publica evento quando número já estava confirmado antes do report")
    void reportNumber_alreadyConfirmed_doesNotPublishEvent() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(5L).phoneNumber("+5511111111111")
                .confirmed(true).reportCount(10).build();

        ReportNumberRequest req = new ReportNumberRequest();
        req.setPhoneNumber("+5511111111111");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findByPhoneNumber(req.getPhoneNumber())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(any(), any())).thenReturn(false);
        when(blockedNumberRepository.save(any(BlockedNumber.class))).thenAnswer(inv -> inv.getArgument(0));

        service.reportNumber(req, user.getEmail());

        verify(eventPublisher, never()).publishEvent(any());
    }

    // ─── reportFalsePositive ─────────────────────────────────────────────────

    @Test
    @DisplayName("reportFalsePositive desconfirma número quando FP * 2 >= reportCount")
    void reportFalsePositive_highFpRatio_deconfirmsNumber() {
        BlockedNumber number = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511111111111")
                .confirmed(true).reportCount(2).falsePositiveCount(0)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findById(1L)).thenReturn(Optional.of(number));
        when(falsePositiveRepository.existsByUserIdAndBlockedNumberId(user.getId(), 1L)).thenReturn(false);

        WhitelistRequest wr = new WhitelistRequest();
        wr.setReason("Falso positivo");
        service.reportFalsePositive(1L, user.getEmail(), wr);

        verify(blockedNumberRepository).save(argThat(bn -> !bn.isConfirmed()));
    }

    @Test
    @DisplayName("reportFalsePositive lança DuplicateReportException em marcação duplicada")
    void reportFalsePositive_duplicate_throwsDuplicateReportException() {
        BlockedNumber number = BlockedNumber.builder().id(1L).build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(blockedNumberRepository.findById(1L)).thenReturn(Optional.of(number));
        when(falsePositiveRepository.existsByUserIdAndBlockedNumberId(user.getId(), 1L)).thenReturn(true);

        WhitelistRequest wr = new WhitelistRequest();
        wr.setReason("FP");
        assertThatThrownBy(() -> service.reportFalsePositive(1L, user.getEmail(), wr))
                .isInstanceOf(DuplicateReportException.class);
    }

    // ─── adminWhitelist / deleteNumber ───────────────────────────────────────

    @Test
    @DisplayName("adminWhitelist lança ResourceNotFoundException quando número não existe")
    void adminWhitelist_notFound_throwsResourceNotFoundException() {
        when(blockedNumberRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.adminWhitelist(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("adminWhitelist chama whitelist(id) no repositório quando número existe")
    void adminWhitelist_found_callsWhitelistOnRepository() {
        when(blockedNumberRepository.existsById(1L)).thenReturn(true);

        service.adminWhitelist(1L);

        verify(blockedNumberRepository).whitelist(1L);
    }

    @Test
    @DisplayName("deleteNumber lança ResourceNotFoundException quando número não existe")
    void deleteNumber_notFound_throwsResourceNotFoundException() {
        when(blockedNumberRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteNumber(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteNumber chama deleteById quando número existe")
    void deleteNumber_found_callsDeleteById() {
        when(blockedNumberRepository.existsById(1L)).thenReturn(true);

        service.deleteNumber(1L);

        verify(blockedNumberRepository).deleteById(1L);
    }

    // ─── getMyReports ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyReports lança UsernameNotFoundException quando email não existe")
    void getMyReports_unknownEmail_throwsUsernameNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyReports("noexist@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("getMyReports retorna lista vazia quando usuário não tem reports")
    void getMyReports_noReports_returnsEmptyList() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(reportRepository.findAllByUserId(user.getId())).thenReturn(List.of());

        var reports = service.getMyReports(user.getEmail());

        assertThat(reports).isEmpty();
    }

    // ─── listConfirmedNumbers ─────────────────────────────────────────────────

    @Test
    @DisplayName("listConfirmedNumbers delega ao repositório e mapeia para DTO")
    void listConfirmedNumbers_delegatesToRepository() {
        BlockedNumber confirmed = BlockedNumber.builder()
                .id(1L).phoneNumber("+5511999990000")
                .confirmed(true).whitelisted(false).reportCount(5)
                .build();

        var pageable = PageRequest.of(0, 10);
        when(blockedNumberRepository.findAllByConfirmedTrueAndWhitelistedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(confirmed)));

        var result = service.listConfirmedNumbers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPhoneNumber()).isEqualTo("+5511999990000");
    }
}
