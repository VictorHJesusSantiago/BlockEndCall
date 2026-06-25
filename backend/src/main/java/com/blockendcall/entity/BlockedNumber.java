package com.blockendcall.entity;

import com.blockendcall.enums.SpamCategory;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blocked_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedNumber implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private SpamCategory category = SpamCategory.UNKNOWN;

    @Column(name = "report_count", nullable = false)
    @Builder.Default
    private int reportCount = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean confirmed = false;

    @Column(name = "false_positive_count", nullable = false)
    @Builder.Default
    private int falsePositiveCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean whitelisted = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "confirmation_count", nullable = false)
    @Builder.Default
    private int confirmationCount = 0;

    @OneToMany(mappedBy = "blockedNumber", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementReportCount() {
        this.reportCount++;
    }

    public void incrementFalsePositive() {
        this.falsePositiveCount++;
    }

    public void incrementConfirmationCount() {
        this.confirmationCount++;
    }

    public int getSpamScore() {
        if (reportCount == 0) return 0;
        int base = Math.min(100, reportCount * 10);
        int penalty = falsePositiveCount * 15;
        return Math.max(0, base - penalty);
    }
}
