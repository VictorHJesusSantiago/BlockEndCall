package com.blockendcall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "block_only_confirmed", nullable = false)
    @Builder.Default
    private boolean blockOnlyConfirmed = false;

    @Column(name = "notify_on_confirm", nullable = false)
    @Builder.Default
    private boolean notifyOnConfirm = true;

    @Column(nullable = false)
    @Builder.Default
    private int sensitivity = 5;

    @Column(name = "paranoia_mode", nullable = false)
    @Builder.Default
    private boolean paranoiaMode = false;

    @Column(name = "block_telemarketing", nullable = false)
    @Builder.Default
    private boolean blockTelemarketing = true;

    @Column(name = "block_scam", nullable = false)
    @Builder.Default
    private boolean blockScam = true;

    @Column(name = "block_robocall", nullable = false)
    @Builder.Default
    private boolean blockRobocall = true;

    @Column(name = "block_silent", nullable = false)
    @Builder.Default
    private boolean blockSilent = false;

    @Column(name = "voicemail_mode", nullable = false)
    @Builder.Default
    private boolean voicemailMode = false;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
