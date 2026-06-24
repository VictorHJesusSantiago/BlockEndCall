package com.blockendcall.repository;

import com.blockendcall.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByUserIdAndBlockedNumberId(Long userId, Long blockedNumberId);

    List<Report> findAllByUserId(Long userId);
}
