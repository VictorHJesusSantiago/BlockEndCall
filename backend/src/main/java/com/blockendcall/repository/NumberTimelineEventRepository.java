package com.blockendcall.repository;

import com.blockendcall.entity.NumberTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NumberTimelineEventRepository extends JpaRepository<NumberTimelineEvent, Long> {

    List<NumberTimelineEvent> findByBlockedNumberIdOrderByCreatedAtDesc(Long numberId);
}
