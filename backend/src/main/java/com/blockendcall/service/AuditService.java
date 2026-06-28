package com.blockendcall.service;

import com.blockendcall.entity.AuditLog;
import com.blockendcall.entity.User;
import com.blockendcall.enums.AuditAction;
import com.blockendcall.repository.AuditLogRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String actorEmail, AuditAction action, String targetType, Long targetId, String details) {
        User actor = null;
        if (actorEmail != null) {
            actor = userRepository.findByEmail(actorEmail).orElse(null);
        }

        AuditLog entry = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build();

        auditLogRepository.save(entry);
        log.info("Audit: {} by {} on {}:{} - {}", action, actorEmail, targetType, targetId, details);
    }
}
