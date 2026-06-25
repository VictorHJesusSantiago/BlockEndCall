package com.blockendcall.entity;

import com.blockendcall.enums.BlockedCallResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_blocked_call_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerBlockedCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "blocked_at", nullable = false)
    @Builder.Default
    private LocalDateTime blockedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "block_result", nullable = false, length = 30)
    @Builder.Default
    private BlockedCallResult blockResult = BlockedCallResult.REJECTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_number_id")
    private BlockedNumber matchedNumber;
}
