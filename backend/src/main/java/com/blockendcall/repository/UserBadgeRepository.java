package com.blockendcall.repository;

import com.blockendcall.entity.UserBadge;
import com.blockendcall.enums.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long userId);

    boolean existsByUserIdAndBadgeType(Long userId, BadgeType type);
}
