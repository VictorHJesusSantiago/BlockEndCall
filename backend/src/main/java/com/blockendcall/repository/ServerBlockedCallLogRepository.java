package com.blockendcall.repository;

import com.blockendcall.entity.ServerBlockedCallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBlockedCallLogRepository extends JpaRepository<ServerBlockedCallLog, Long> {

    Page<ServerBlockedCallLog> findByUserId(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
