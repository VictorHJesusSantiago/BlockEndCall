package com.blockendcall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_whitelist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicWhitelist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 200)
    private String organization;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    private User addedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
