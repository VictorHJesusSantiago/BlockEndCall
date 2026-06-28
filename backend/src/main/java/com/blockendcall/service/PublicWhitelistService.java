package com.blockendcall.service;

import com.blockendcall.dto.request.AddPublicWhitelistRequest;
import com.blockendcall.dto.response.PublicWhitelistResponse;
import com.blockendcall.entity.PublicWhitelist;
import com.blockendcall.entity.User;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.PublicWhitelistRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicWhitelistService {

    private final PublicWhitelistRepository publicWhitelistRepository;
    private final UserRepository userRepository;

    public Page<PublicWhitelistResponse> listVerified(Pageable pageable) {
        return publicWhitelistRepository.findByVerifiedTrue(pageable)
                .map(PublicWhitelistResponse::from);
    }

    public Optional<PublicWhitelistResponse> findByPhone(String phone) {
        return publicWhitelistRepository.findByPhoneNumber(phone)
                .map(PublicWhitelistResponse::from);
    }

    @Transactional
    public PublicWhitelistResponse add(AddPublicWhitelistRequest req, String adminEmail) {
        User addedBy = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + adminEmail));

        PublicWhitelist entry = PublicWhitelist.builder()
                .phoneNumber(req.getPhoneNumber())
                .organization(req.getOrganization())
                .category(req.getCategory())
                .verified(req.isVerified())
                .addedBy(addedBy)
                .build();

        return PublicWhitelistResponse.from(publicWhitelistRepository.save(entry));
    }

    @Transactional
    public void verify(Long id) {
        PublicWhitelist entry = publicWhitelistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Public whitelist entry not found: " + id));
        entry.setVerified(true);
        publicWhitelistRepository.save(entry);
    }
}
