package com.blockendcall.repository;

import com.blockendcall.entity.NumberReportedName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NumberReportedNameRepository extends JpaRepository<NumberReportedName, Long> {

    List<NumberReportedName> findByBlockedNumberIdOrderByReportCountDesc(Long numberId);

    Optional<NumberReportedName> findByBlockedNumberIdAndReportedName(Long numberId, String reportedName);
}
