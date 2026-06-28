package com.blockendcall.service;

import com.blockendcall.dto.request.AdminSuspendRequest;
import com.blockendcall.dto.request.PromoteUserRequest;
import com.blockendcall.dto.response.AdminUserResponse;
import com.blockendcall.dto.response.BlockedNumberResponse;
import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.entity.User;
import com.blockendcall.enums.AuditAction;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.BlockedNumberRepository;
import com.blockendcall.repository.ReportRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BlockedNumberRepository blockedNumberRepository;
    private final ReportRepository reportRepository;
    private final AuditService auditService;

    public Page<AdminUserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> AdminUserResponse.from(user, reportRepository.countByUserId(user.getId())));
    }

    @Transactional
    public void suspendUser(AdminSuspendRequest req, String adminEmail) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));
        user.setSuspended(req.isSuspend());
        userRepository.save(user);
        auditService.log(adminEmail, AuditAction.USER_SUSPEND, "User", req.getUserId(),
                "Suspended: " + req.isSuspend() + " - " + req.getReason());
    }

    @Transactional
    public void unsuspendUser(Long userId, String adminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setSuspended(false);
        userRepository.save(user);
        auditService.log(adminEmail, AuditAction.USER_UNSUSPEND, "User", userId, "User unsuspended");
    }

    @Transactional
    public void promoteUser(PromoteUserRequest req, String adminEmail) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));
        user.setRole(req.getNewRole());
        userRepository.save(user);
        auditService.log(adminEmail, AuditAction.USER_PROMOTE, "User", req.getUserId(),
                "Promoted to role: " + req.getNewRole());
    }

    @Transactional
    public void bulkApprove(List<Long> ids, String adminEmail) {
        for (Long id : ids) {
            blockedNumberRepository.findById(id).ifPresent(number -> {
                number.setConfirmed(true);
                blockedNumberRepository.save(number);
                auditService.log(adminEmail, AuditAction.NUMBER_APPROVE, "BlockedNumber", id, "Bulk approved");
            });
        }
    }

    @Transactional
    public void bulkReject(List<Long> ids, String adminEmail) {
        for (Long id : ids) {
            if (blockedNumberRepository.existsById(id)) {
                blockedNumberRepository.deleteById(id);
                auditService.log(adminEmail, AuditAction.NUMBER_REJECT, "BlockedNumber", id, "Bulk rejected and deleted");
            }
        }
    }

    public Page<BlockedNumberResponse> getPendingNumbers(Pageable pageable) {
        return blockedNumberRepository.findAllByConfirmedFalseAndWhitelistedFalse(pageable)
                .map(BlockedNumberResponse::from);
    }
}
