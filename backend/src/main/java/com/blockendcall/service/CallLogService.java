package com.blockendcall.service;

import com.blockendcall.dto.request.LogCallRequest;
import com.blockendcall.dto.response.BlockedCallLogResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.ServerBlockedCallLog;
import com.blockendcall.entity.User;
import com.blockendcall.enums.BlockedCallResult;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.ServerBlockedCallLogRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CallLogService {

    private final ServerBlockedCallLogRepository serverBlockedCallLogRepository;
    private final BlockedNumberRepository blockedNumberRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logBlockedCall(LogCallRequest req, String userEmail) {
        User user = findUser(userEmail);

        BlockedNumber matchedNumber = null;
        if (req.getMatchedNumberId() != null) {
            matchedNumber = blockedNumberRepository.findById(req.getMatchedNumberId()).orElse(null);
        }

        BlockedCallResult result = req.getBlockResult() != null ? req.getBlockResult() : BlockedCallResult.REJECTED;

        serverBlockedCallLogRepository.save(ServerBlockedCallLog.builder()
                .user(user)
                .phoneNumber(req.getPhoneNumber())
                .blockResult(result)
                .matchedNumber(matchedNumber)
                .build());
    }

    public Page<BlockedCallLogResponse> getMyCallLog(String email, Pageable pageable) {
        User user = findUser(email);
        return serverBlockedCallLogRepository.findByUserId(user.getId(), pageable)
                .map(BlockedCallLogResponse::from);
    }

    public long getMyBlockedCallCount(String email) {
        User user = findUser(email);
        return serverBlockedCallLogRepository.countByUserId(user.getId());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
