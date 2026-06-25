package com.blockendcall.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "number_reported_names",
    uniqueConstraints = @UniqueConstraint(columnNames = {"blocked_number_id", "reported_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NumberReportedName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_number_id", nullable = false)
    private BlockedNumber blockedNumber;

    @Column(name = "reported_name", nullable = false, length = 200)
    private String reportedName;

    @Column(name = "report_count", nullable = false)
    @Builder.Default
    private int reportCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
