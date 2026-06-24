package com.blockendcall.service;

import com.blockendcall.dto.request.ReportNumberRequest;
import com.blockendcall.dto.request.WhitelistRequest;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.NumberCheckResponse;
import com.blockendcall.dto.response.UserReportResponse;
import com.blockendcall.entity.*;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockedNumberService {

    private final BlockedNumberRepository blockedNumberRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FalsePositiveRepository falsePositiveRepository;

    @Value("${app.report.threshold:5}")
    private int reportThreshold;

    @Cacheable(value = "number-check", key = "#phoneNumber")
    public NumberCheckResponse checkNumber(String phoneNumber) {
        return blockedNumberRepository.findByPhoneNumber(phoneNumber)
                .filter(n -> !n.isWhitelisted())
                .map(BlockedNumberResponse::from)
                .map(NumberCheckResponse::from)
                .orElse(NumberCheckResponse.safe(phoneNumber));
    }

    public Page<BlockedNumberResponse> listConfirmedNumbers(Pageable pageable) {
        return blockedNumberRepository.findAllByConfirmedTrueAndWhitelistedFalse(pageable)
                .map(BlockedNumberResponse::from);
    }

    public Page<BlockedNumberResponse> searchNumbers(String query, Pageable pageable) {
        return blockedNumberRepository.searchByPhoneNumber(query.trim(), pageable)
                .map(BlockedNumberResponse::from);
    }

    public Page<BlockedNumberResponse> listByCategory(SpamCategory category, Pageable pageable) {
        return blockedNumberRepository.findAllByCategoryAndConfirmedTrue(category, pageable)
                .map(BlockedNumberResponse::from);
    }

    public BlockedNumberResponse getById(Long id) {
        BlockedNumber number = blockedNumberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Number not found: " + id));
        return BlockedNumberResponse.from(number);
    }

    public List<UserReportResponse> getMyReports(String userEmail) {
        User user = findUser(userEmail);
        return reportRepository.findAllByUserId(user.getId()).stream()
                .map(UserReportResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"number-check", "global-stats"}, allEntries = true)
    public BlockedNumberResponse reportNumber(ReportNumberRequest request, String userEmail) {
        User user = findUser(userEmail);

        BlockedNumber blockedNumber = blockedNumberRepository
                .findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> BlockedNumber.builder()
                        .phoneNumber(request.getPhoneNumber())
                        .category(request.getCategory())
                        .description(request.getDescription())
                        .reportCount(0)
                        .build());

        if (blockedNumber.getId() != null
                && reportRepository.existsByUserIdAndBlockedNumberId(user.getId(), blockedNumber.getId())) {
            throw new DuplicateReportException("Você já reportou este número");
        }

        blockedNumber.incrementReportCount();

        if (!blockedNumber.isWhitelisted() && blockedNumber.getReportCount() >= reportThreshold) {
            blockedNumber.setConfirmed(true);
        }

        BlockedNumber saved = blockedNumberRepository.save(blockedNumber);

        reportRepository.save(Report.builder()
                .user(user)
                .blockedNumber(saved)
                .description(request.getDescription())
                .build());

        return BlockedNumberResponse.from(saved);
    }

    @Transactional
    @CacheEvict(value = {"number-check", "global-stats"}, allEntries = true)
    public void reportFalsePositive(Long numberId, String userEmail, WhitelistRequest request) {
        User user = findUser(userEmail);
        BlockedNumber number = blockedNumberRepository.findById(numberId)
                .orElseThrow(() -> new ResourceNotFoundException("Number not found: " + numberId));

        if (falsePositiveRepository.existsByUserIdAndBlockedNumberId(user.getId(), numberId)) {
            throw new DuplicateReportException("Você já marcou este número como falso positivo");
        }

        falsePositiveRepository.save(FalsePositiveReport.builder()
                .user(user)
                .blockedNumber(number)
                .reason(request.getReason())
                .build());

        number.incrementFalsePositive();

        // Auto-whitelist if false positives exceed half of spam reports
        if (number.getFalsePositiveCount() * 2 >= number.getReportCount()) {
            number.setConfirmed(false);
        }

        blockedNumberRepository.save(number);
    }

    @Transactional
    @CacheEvict(value = {"number-check", "global-stats"}, allEntries = true)
    public void adminWhitelist(Long id) {
        if (!blockedNumberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Number not found: " + id);
        }
        blockedNumberRepository.whitelist(id);
    }

    @Transactional
    @CacheEvict(value = {"number-check", "global-stats"}, allEntries = true)
    public void deleteNumber(Long id) {
        if (!blockedNumberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Number not found: " + id);
        }
        blockedNumberRepository.deleteById(id);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
