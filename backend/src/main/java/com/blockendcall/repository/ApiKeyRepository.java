package com.blockendcall.repository;

import com.blockendcall.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyValue(String keyValue);

    List<ApiKey> findByUserId(Long userId);

    Optional<ApiKey> findByIdAndUserId(Long id, Long userId);
}
