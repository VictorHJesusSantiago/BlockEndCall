package com.blockendcall.repository;

import com.blockendcall.entity.PersonalWhitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalWhitelistRepository extends JpaRepository<PersonalWhitelist, Long> {

    List<PersonalWhitelist> findByUserId(Long userId);

    boolean existsByUserIdAndPhoneNumber(Long userId, String phoneNumber);

    Optional<PersonalWhitelist> findByUserIdAndPhoneNumber(Long userId, String phoneNumber);

    @Modifying
    @Transactional
    void deleteByUserIdAndPhoneNumber(Long userId, String phoneNumber);
}
