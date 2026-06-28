package com.blockendcall.repository;

import com.blockendcall.entity.ReportConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportConfirmationRepository extends JpaRepository<ReportConfirmation, Long> {

    boolean existsByUserIdAndBlockedNumberId(Long userId, Long numberId);

    long countByBlockedNumberId(Long numberId);
}
