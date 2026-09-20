package com.blockendcall.repository;

import com.blockendcall.entity.Report;
import com.blockendcall.entity.BlockedNumber;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByUserIdAndBlockedNumberId(Long userId, Long blockedNumberId);

    List<Report> findAllByUserId(Long userId);

    long countByUserId(Long userId);

    @Query("""
            SELECT DISTINCT r.blockedNumber
            FROM Report r
            WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    List<BlockedNumber> searchByDescription(String query, Pageable pageable);

    @Query(value = """
            SELECT CAST(created_at AS date) AS report_day, COUNT(*) AS report_count
            FROM reports
            WHERE created_at >= :since
            GROUP BY CAST(created_at AS date)
            ORDER BY report_day
            """, nativeQuery = true)
    List<Object[]> countByDay(LocalDateTime since);

    @Query(value = """
            SELECT EXTRACT(HOUR FROM created_at) AS report_hour, COUNT(*) AS report_count
            FROM reports
            GROUP BY EXTRACT(HOUR FROM created_at)
            ORDER BY report_hour
            """, nativeQuery = true)
    List<Object[]> countByHour();
}
