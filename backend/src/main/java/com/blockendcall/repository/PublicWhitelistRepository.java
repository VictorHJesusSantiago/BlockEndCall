package com.blockendcall.repository;

import com.blockendcall.entity.PublicWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicWhitelistRepository extends JpaRepository<PublicWhitelist, Long> {

    Optional<PublicWhitelist> findByPhoneNumber(String phoneNumber);

    Page<PublicWhitelist> findByVerifiedTrue(Pageable pageable);
}
