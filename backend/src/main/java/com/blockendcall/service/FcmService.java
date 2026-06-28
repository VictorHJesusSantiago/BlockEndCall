package com.blockendcall.service;

import com.blockendcall.entity.FcmToken;
import com.blockendcall.entity.User;
import com.blockendcall.repository.FcmTokenRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerToken(String token, String deviceId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!fcmTokenRepository.existsByUserIdAndToken(user.getId(), token)) {
            fcmTokenRepository.save(FcmToken.builder()
                    .user(user)
                    .token(token)
                    .deviceId(deviceId)
                    .build());
            log.info("Registered FCM token for user {}", email);
        }
    }

    public void sendNotification(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        // Stub implementation — in production, would call Firebase Admin SDK
        log.info("Sending notification to user {} ({} devices): {} - {}",
                userId, tokens.size(), title, body);
    }
}
