package com.blockendcall.service;

import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.dto.response.StatsResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final BlockedNumberRepository blockedNumberRepository;
    private final UserRepository userRepository;

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

    private Map<String, Long> buildCategoryMap() {
        Map<String, Long> result = new LinkedHashMap<>();
        blockedNumberRepository.countByCategory()
                .forEach(row -> result.put(row[0].toString(), (Long) row[1]));
        return result;
    }
}
