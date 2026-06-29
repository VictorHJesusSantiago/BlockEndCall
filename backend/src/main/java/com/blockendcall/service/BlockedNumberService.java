package com.blockendcall.service;

import com.blockendcall.dto.request.ReportNumberRequest;
import com.blockendcall.dto.request.WhitelistRequest;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.NumberCheckResponse;
import com.blockendcall.dto.response.UserReportResponse;
import com.blockendcall.entity.*;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.event.NumberConfirmedEvent;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockedNumberService {

    private final BlockedNumberRepository blockedNumberRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final FalsePositiveRepository falsePositiveRepository;
    private final PersonalWhitelistRepository personalWhitelistRepository;
    private final PersonalBlacklistRepository personalBlacklistRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        boolean wasConfirmed = blockedNumber.isConfirmed();
        if (!blockedNumber.isWhitelisted() && blockedNumber.getReportCount() >= reportThreshold) {
            blockedNumber.setConfirmed(true);
        }

        BlockedNumber saved = blockedNumberRepository.save(blockedNumber);
        if (!wasConfirmed && saved.isConfirmed()) {
            // Publish as a Spring event rather than calling WebhookService directly.
            // WebhookService listens with @TransactionalEventListener(AFTER_COMMIT),
            // so webhook delivery only happens after this transaction successfully commits.
            // Primitive data is extracted here, inside the active session, to avoid
            // any detached-entity access on the async webhook thread.
            eventPublisher.publishEvent(new NumberConfirmedEvent(
                    saved.getId(),
                    saved.getPhoneNumber(),
                    saved.getCategory() != null ? saved.getCategory().name() : "UNKNOWN",
                    saved.getReportCount()
            ));
        }

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

    public NumberCheckResponse getEnhancedCheck(String phoneNumber, String userEmail) {
        User user = findUser(userEmail);
        NumberCheckResponse base = checkNumber(phoneNumber);
        boolean inPersonalWhitelist = personalWhitelistRepository.existsByUserIdAndPhoneNumber(user.getId(), phoneNumber);
        return NumberCheckResponse.builder()
                .phoneNumber(base.getPhoneNumber())
                .blocked(base.isBlocked())
                .confirmed(base.isConfirmed())
                .category(base.getCategory())
                .reportCount(base.getReportCount())
                .spamScore(base.getSpamScore())
                .description(base.getDescription())
                .riskLevel(base.getRiskLevel())
                .inPersonalWhitelist(inPersonalWhitelist)
                .build();
    }

    @Transactional
    public void confirmMeToo(Long numberId, String userEmail) {
        BlockedNumber number = blockedNumberRepository.findById(numberId)
                .orElseThrow(() -> new ResourceNotFoundException("Number not found: " + numberId));
        number.setConfirmationCount(number.getConfirmationCount() + 1);
        blockedNumberRepository.save(number);
    }

    public List<NumberCheckResponse> checkBatch(List<String> phoneNumbers) {
        return phoneNumbers.stream()
                .map(this::checkNumber)
                .collect(Collectors.toList());
    }

    public List<String> autocomplete(String prefix) {
        return blockedNumberRepository.autocomplete(prefix, PageRequest.of(0, 10));
    }

    public List<BlockedNumberResponse> searchByDescription(String query, Pageable pageable) {
        return reportRepository.searchByDescription(query, pageable).stream()
                .map(BlockedNumberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Integer> importFromCsv(MultipartFile file) {
        int imported = 0, skipped = 0, errors = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine && line.toLowerCase().startsWith("phone")) { firstLine = false; continue; }
                firstLine = false;
                String[] parts = line.split(",", 2);
                String phone = parts[0].trim().replaceAll("[^+\\d]", "");
                if (phone.isEmpty()) { skipped++; continue; }
                SpamCategory cat = SpamCategory.UNKNOWN;
                if (parts.length > 1) {
                    try { cat = SpamCategory.valueOf(parts[1].trim().toUpperCase()); } catch (Exception ignored) {}
                }
                try {
                    BlockedNumber bn = blockedNumberRepository.findByPhoneNumber(phone)
                            .orElseGet(() -> BlockedNumber.builder()
                                    .phoneNumber(phone).category(cat).reportCount(0).build());
                    bn.setConfirmed(true);
                    if (bn.getReportCount() < reportThreshold) bn.setReportCount(reportThreshold);
                    blockedNumberRepository.save(bn);
                    imported++;
                } catch (Exception e) { errors++; }
            }
        } catch (Exception e) { errors++; }
        Map<String, Integer> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("errors", errors);
        return result;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
