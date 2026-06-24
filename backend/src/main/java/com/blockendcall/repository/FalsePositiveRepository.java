package com.blockendcall.repository;

import com.blockendcall.entity.FalsePositiveReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FalsePositiveRepository extends JpaRepository<FalsePositiveReport, Long> {

    boolean existsByUserIdAndBlockedNumberId(Long userId, Long blockedNumberId);
}
