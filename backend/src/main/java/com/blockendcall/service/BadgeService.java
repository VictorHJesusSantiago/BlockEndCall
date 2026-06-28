package com.blockendcall.service;

import com.blockendcall.dto.response.BadgeResponse;
import com.blockendcall.entity.User;
import com.blockendcall.entity.UserBadge;
import com.blockendcall.enums.BadgeType;
import com.blockendcall.repository.ReportRepository;
import com.blockendcall.repository.UserBadgeRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkAndAwardBadges(Long userId) {
        long count = reportRepository.countByUserId(userId);

        if (count >= 1) {
            awardBadge(userId, BadgeType.FIRST_REPORT);
        }
        if (count >= 10) {
            awardBadge(userId, BadgeType.REPORTER_10);
        }
        if (count >= 50) {
            awardBadge(userId, BadgeType.REPORTER_50);
        }
        if (count >= 100) {
            awardBadge(userId, BadgeType.REPORTER_100);
        }
        if (count >= 500) {
            awardBadge(userId, BadgeType.REPORTER_500);
        }
    }

    @Transactional
    public void awardBadge(Long userId, BadgeType type) {
        if (!userBadgeRepository.existsByUserIdAndBadgeType(userId, type)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            UserBadge badge = UserBadge.builder()
                    .user(user)
                    .badgeType(type)
                    .build();
            userBadgeRepository.save(badge);

            user.setReputationScore(user.getReputationScore() + 10);
            userRepository.save(user);

            log.info("Awarded badge {} to user {}", type, userId);
        }
    }

    public List<BadgeResponse> getBadges(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return userBadgeRepository.findByUserId(user.getId()).stream()
                .map(BadgeResponse::from)
                .collect(Collectors.toList());
    }
}
