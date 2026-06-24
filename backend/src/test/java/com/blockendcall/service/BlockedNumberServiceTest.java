package com.blockendcall.service;

import com.blockendcall.dto.request.ReportNumberRequest;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.Report;
import com.blockendcall.entity.User;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.ReportRepository;
import com.blockendcall.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BlockedNumberServiceTest {

    @Mock BlockedNumberRepository blockedNumberRepository;
    @Mock ReportRepository reportRepository;
    @Mock UserRepository userRepository;

    @InjectMocks
    BlockedNumberService service;

    private User testUser;
    private ReportNumberRequest reportRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "reportThreshold", 5);

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("hash")
                .build();

        reportRequest = new ReportNumberRequest();
        reportRequest.setPhoneNumber("+5511999990000");
        reportRequest.setCategory(SpamCategory.TELEMARKETING);
        reportRequest.setDescription("Annoying calls");
    }

    @Test
    @DisplayName("checkNumber returns empty when number is not blocked")
    void checkNumber_notFound_returnsEmpty() {
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        Optional<BlockedNumberResponse> result = service.checkNumber("+5511999990000");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("checkNumber returns empty when number exists but is not confirmed")
    void checkNumber_notConfirmed_returnsEmpty() {
        BlockedNumber number = BlockedNumber.builder()
                .phoneNumber("+5511999990000")
                .confirmed(false)
                .build();
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(number));

        Optional<BlockedNumberResponse> result = service.checkNumber("+5511999990000");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("checkNumber returns response when number is confirmed")
    void checkNumber_confirmed_returnsResponse() {
        BlockedNumber number = BlockedNumber.builder()
                .id(1L)
                .phoneNumber("+5511999990000")
                .confirmed(true)
                .category(SpamCategory.SCAM)
                .reportCount(5)
                .build();
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(number));

        Optional<BlockedNumberResponse> result = service.checkNumber("+5511999990000");

        assertThat(result).isPresent();
        assertThat(result.get().getPhoneNumber()).isEqualTo("+5511999990000");
    }

    @Test
    @DisplayName("reportNumber creates new blocked number on first report")
    void reportNumber_newNumber_createsEntry() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());
        when(blockedNumberRepository.save(any())).thenAnswer(inv -> {
            BlockedNumber bn = inv.getArgument(0);
            ReflectionTestUtils.setField(bn, "id", 1L);
            return bn;
        });
        when(reportRepository.save(any())).thenReturn(new Report());

        BlockedNumberResponse response = service.reportNumber(reportRequest, testUser.getEmail());

        assertThat(response.getPhoneNumber()).isEqualTo(reportRequest.getPhoneNumber());
        assertThat(response.getReportCount()).isEqualTo(1);
        verify(blockedNumberRepository).save(any());
        verify(reportRepository).save(any());
    }

    @Test
    @DisplayName("reportNumber auto-confirms when threshold is reached")
    void reportNumber_thresholdReached_confirmsNumber() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(1L)
                .phoneNumber("+5511999990000")
                .reportCount(4)
                .confirmed(false)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(any(), any())).thenReturn(false);
        when(blockedNumberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reportRepository.save(any())).thenReturn(new Report());

        BlockedNumberResponse response = service.reportNumber(reportRequest, testUser.getEmail());

        assertThat(response.isConfirmed()).isTrue();
        assertThat(response.getReportCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("reportNumber throws when user already reported the same number")
    void reportNumber_duplicate_throwsException() {
        BlockedNumber existing = BlockedNumber.builder()
                .id(1L)
                .phoneNumber("+5511999990000")
                .reportCount(2)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(testUser));
        when(blockedNumberRepository.findByPhoneNumber(any())).thenReturn(Optional.of(existing));
        when(reportRepository.existsByUserIdAndBlockedNumberId(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.reportNumber(reportRequest, testUser.getEmail()))
                .isInstanceOf(DuplicateReportException.class);
    }
}
