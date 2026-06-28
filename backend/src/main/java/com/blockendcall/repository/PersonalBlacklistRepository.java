package com.blockendcall.repository;

import com.blockendcall.entity.PersonalBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalBlacklistRepository extends JpaRepository<PersonalBlacklist, Long> {

    List<PersonalBlacklist> findByUserId(Long userId);

    boolean existsByUserIdAndPhoneNumber(Long userId, String phoneNumber);

    Optional<PersonalBlacklist> findByUserIdAndPhoneNumber(Long userId, String phoneNumber);

    @Modifying
    @Transactional
    void deleteByUserIdAndPhoneNumber(Long userId, String phoneNumber);
}
