package com.blockendcall.service;

import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.repository.AuditLogRepository;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final BlockedNumberRepository blockedNumberRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final BadgeService badgeService;

    /**
     * Every day at 03:00 — unconfirm old pending numbers with fewer than 3 reports
     * that have not been updated in 6 months.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void autoExpireOldReports() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);
        List<BlockedNumber> expired = blockedNumberRepository.findExpiredPending(threshold, 3);

        for (BlockedNumber number : expired) {
            number.setConfirmed(false);
            blockedNumberRepository.save(number);
        }

        if (!expired.isEmpty()) {
            log.info("Auto-expired {} old pending numbers", expired.size());
        }
    }

    /**
     * Every day at 04:00 — auto-confirm numbers that have been reported by 10+ different users
     * in the last 24 hours and are not yet confirmed.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void detectCampaigns() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<BlockedNumber> candidates = blockedNumberRepository
                .findRecentlyReportedAboveThreshold(10, since);

        for (BlockedNumber number : candidates) {
            if (!number.isConfirmed() && !number.isWhitelisted()) {
                number.setConfirmed(true);
                blockedNumberRepository.save(number);
                log.info("Auto-confirmed campaign number: {}", number.getPhoneNumber());
            }
        }
    }

    /**
     * Every Monday at 02:00 — delete audit log entries older than 1 year.
     */
    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    public void cleanOldAuditLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(1);
        long deleted = auditLogRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Deleted {} old audit log entries", deleted);
    }
}
