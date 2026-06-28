package com.blockendcall.repository;

import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.enums.SpamCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedNumberRepository extends JpaRepository<BlockedNumber, Long> {

    Optional<BlockedNumber> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<BlockedNumber> findAllByConfirmedTrueAndWhitelistedFalse(Pageable pageable);

    Page<BlockedNumber> findAllByCategoryAndConfirmedTrue(SpamCategory category, Pageable pageable);

    @Query("SELECT b FROM BlockedNumber b WHERE LOWER(b.phoneNumber) LIKE LOWER(CONCAT('%',:q,'%')) AND b.confirmed = true")
    Page<BlockedNumber> searchByPhoneNumber(@Param("q") String query, Pageable pageable);

    @Query("SELECT b FROM BlockedNumber b WHERE b.updatedAt >= :since AND b.confirmed = true AND b.whitelisted = false ORDER BY b.reportCount DESC")
    List<BlockedNumber> findTrending(@Param("since") LocalDateTime since, Pageable pageable);

    Page<BlockedNumber> findAllByConfirmedTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT b.category, COUNT(b) FROM BlockedNumber b WHERE b.confirmed = true GROUP BY b.category")
    List<Object[]> countByCategory();

    @Query("SELECT COALESCE(SUM(b.reportCount), 0) FROM BlockedNumber b")
    long sumAllReports();

    @Query("SELECT COUNT(b) FROM BlockedNumber b WHERE b.confirmed = true")
    long countConfirmed();

    @Query("SELECT COUNT(b) FROM BlockedNumber b WHERE b.confirmed = false AND b.whitelisted = false")
    long countPending();

    @Modifying
    @Query("UPDATE BlockedNumber b SET b.confirmed = true WHERE b.reportCount >= :threshold AND b.confirmed = false AND b.whitelisted = false")
    int confirmNumbersAboveThreshold(int threshold);

    @Modifying
    @Query("UPDATE BlockedNumber b SET b.whitelisted = true, b.confirmed = false WHERE b.id = :id")
    void whitelist(@Param("id") Long id);

    Page<BlockedNumber> findAllByConfirmedFalseAndWhitelistedFalse(Pageable pageable);

    @Query("SELECT SUBSTRING(b.phoneNumber, 1, 2) as ddd, COUNT(b) as cnt FROM BlockedNumber b WHERE b.confirmed = true GROUP BY SUBSTRING(b.phoneNumber, 1, 2)")
    List<Object[]> countByDdd();

    @Query("SELECT b FROM BlockedNumber b WHERE b.reportCount >= :minReports AND b.confirmed = false AND b.whitelisted = false AND b.createdAt >= :since")
    List<BlockedNumber> findRecentlyReportedAboveThreshold(@Param("minReports") int minReports, @Param("since") LocalDateTime since);

    @Query("SELECT b FROM BlockedNumber b WHERE b.confirmed = false AND b.whitelisted = false AND b.updatedAt < :before AND b.reportCount < :minCount")
    List<BlockedNumber> findExpiredPending(@Param("before") LocalDateTime before, @Param("minCount") int minCount);

    @Query("SELECT b FROM BlockedNumber b WHERE b.confirmed = true AND b.createdAt >= :since ORDER BY b.reportCount DESC")
    List<BlockedNumber> findTopByPeriod(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT b.phoneNumber FROM BlockedNumber b WHERE b.phoneNumber LIKE CONCAT(:prefix,'%') AND b.confirmed = true ORDER BY b.reportCount DESC")
    List<String> autocomplete(@Param("prefix") String prefix, Pageable pageable);
}
