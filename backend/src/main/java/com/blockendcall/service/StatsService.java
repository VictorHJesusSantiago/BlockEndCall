package com.blockendcall.service;

import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.EnhancedStatsResponse;
import com.blockendcall.dto.response.LeaderboardEntry;
import com.blockendcall.dto.response.StatsResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.User;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.ReportRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final BlockedNumberRepository blockedNumberRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Cacheable(value = "global-stats")
    public StatsResponse getGlobalStats() {
        long totalConfirmed = blockedNumberRepository.countConfirmed();
        long totalPending = blockedNumberRepository.countPending();
        long totalReports = blockedNumberRepository.sumAllReports();
        long totalUsers = userRepository.count();

        Map<String, Long> byCategory = buildCategoryMap();

        List<BlockedNumberResponse> trending = blockedNumberRepository
                .findTrending(LocalDateTime.now().minusDays(7), PageRequest.of(0, 5))
                .stream()
                .map(BlockedNumberResponse::from)
                .collect(Collectors.toList());

        List<BlockedNumberResponse> recentlyAdded = blockedNumberRepository
                .findAllByConfirmedTrueOrderByCreatedAtDesc(PageRequest.of(0, 5))
                .stream()
                .map(BlockedNumberResponse::from)
                .collect(Collectors.toList());

        return StatsResponse.builder()
                .totalConfirmed(totalConfirmed)
                .totalPending(totalPending)
                .totalReports(totalReports)
                .totalUsers(totalUsers)
                .byCategory(byCategory)
                .trending(trending)
                .recentlyAdded(recentlyAdded)
                .build();
    }

    public Map<String, Long> getStatsByDdd() {
        Map<String, Long> result = new LinkedHashMap<>();
        blockedNumberRepository.countByDdd()
                .forEach(row -> result.put(row[0] != null ? row[0].toString() : "??", (Long) row[1]));
        return result;
    }

    public List<LeaderboardEntry> getLeaderboard(int limit) {
        List<User> users = userRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "reputationScore"))).getContent();

        AtomicInteger rank = new AtomicInteger(1);
        return users.stream()
                .map(user -> LeaderboardEntry.builder()
                        .userId(user.getId())
                        .name(user.getName())
                        .totalReports((int) reportRepository.countByUserId(user.getId()))
                        .reputationScore(user.getReputationScore())
                        .rank(rank.getAndIncrement())
                        .build())
                .collect(Collectors.toList());
    }

    public List<BlockedNumberResponse> getTopByPeriod(String period, int limit) {
        LocalDateTime since;
        switch (period.toUpperCase()) {
            case "DAILY":   since = LocalDateTime.now().minusDays(1); break;
            case "MONTHLY": since = LocalDateTime.now().minusDays(30); break;
            default:        since = LocalDateTime.now().minusDays(7); break;
        }
        return blockedNumberRepository.findTopByPeriod(since, PageRequest.of(0, Math.min(limit, 50)))
                .stream()
                .map(BlockedNumberResponse::from)
                .collect(Collectors.toList());
    }

    public EnhancedStatsResponse getEnhancedStats() {
        StatsResponse base = getGlobalStats();

        Map<String, Long> byDdd = getStatsByDdd();

        // Real daily counts — last 30 days
        List<Object[]> dayRows = reportRepository.countByDay(
                LocalDateTime.now().minusDays(29).truncatedTo(ChronoUnit.DAYS));
        Map<String, Long> dayMap = new HashMap<>();
        for (Object[] row : dayRows) {
            String dateStr;
            if (row[0] instanceof java.sql.Date) {
                dateStr = ((java.sql.Date) row[0]).toLocalDate().format(DATE_FMT);
            } else if (row[0] instanceof LocalDate) {
                dateStr = ((LocalDate) row[0]).format(DATE_FMT);
            } else {
                dateStr = row[0].toString().substring(0, 10);
            }
            dayMap.put(dateStr, ((Number) row[1]).longValue());
        }

        List<EnhancedStatsResponse.DailyCount> dailyCounts = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(DATE_FMT);
            dailyCounts.add(EnhancedStatsResponse.DailyCount.builder()
                    .date(dateStr)
                    .count(dayMap.getOrDefault(dateStr, 0L))
                    .build());
        }

        // Real peak hours (0-23)
        List<Object[]> hourRows = reportRepository.countByHour();
        Map<Integer, Long> peakHours = new LinkedHashMap<>();
        Map<Integer, Long> hourMap = new HashMap<>();
        for (Object[] row : hourRows) {
            hourMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        for (int h = 0; h < 24; h++) {
            peakHours.put(h, hourMap.getOrDefault(h, 0L));
        }

        long totalReports = base.getTotalReports();
        long totalConfirmed = base.getTotalConfirmed();

        // Real false positive rate using actual falsePositiveCount data
        long totalFalsePositives = blockedNumberRepository.findAll().stream()
                .mapToLong(BlockedNumber::getFalsePositiveCount).sum();
        double falsePositiveRate = totalReports > 0
                ? Math.round((double) totalFalsePositives / totalReports * 10000.0) / 100.0
                : 0.0;
        double accuracyRate = Math.round((100.0 - falsePositiveRate) * 100.0) / 100.0;

        return EnhancedStatsResponse.builder()
                .totalConfirmed(base.getTotalConfirmed())
                .totalPending(base.getTotalPending())
                .totalReports(base.getTotalReports())
                .totalUsers(base.getTotalUsers())
                .byCategory(base.getByCategory())
                .trending(base.getTrending())
                .recentlyAdded(base.getRecentlyAdded())
                .byDdd(byDdd)
                .dailyCounts(dailyCounts)
                .peakHours(peakHours)
                .falsePositiveRate(falsePositiveRate)
                .accuracyRate(accuracyRate)
                .build();
    }

    private Map<String, Long> buildCategoryMap() {
        Map<String, Long> result = new LinkedHashMap<>();
        blockedNumberRepository.countByCategory()
                .forEach(row -> result.put(row[0].toString(), (Long) row[1]));
        return result;
    }
}
