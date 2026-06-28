package com.blockendcall.service;

import com.blockendcall.dto.request.PersonalListRequest;
import com.blockendcall.dto.response.PersonalListResponse;
import com.blockendcall.entity.PersonalBlacklist;
import com.blockendcall.entity.PersonalWhitelist;
import com.blockendcall.entity.User;
import com.blockendcall.exception.DuplicateReportException;
import com.blockendcall.repository.PersonalBlacklistRepository;
import com.blockendcall.repository.PersonalWhitelistRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalListService {

    private final PersonalWhitelistRepository personalWhitelistRepository;
    private final PersonalBlacklistRepository personalBlacklistRepository;
    private final UserRepository userRepository;

    public List<PersonalListResponse> getMyWhitelist(String email) {
        User user = findUser(email);
        return personalWhitelistRepository.findByUserId(user.getId()).stream()
                .map(PersonalListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalListResponse addToWhitelist(PersonalListRequest req, String email) {
        User user = findUser(email);
        if (personalWhitelistRepository.existsByUserIdAndPhoneNumber(user.getId(), req.getPhoneNumber())) {
            throw new DuplicateReportException("Número já está na whitelist pessoal");
        }
        PersonalWhitelist saved = personalWhitelistRepository.save(PersonalWhitelist.builder()
                .user(user)
                .phoneNumber(req.getPhoneNumber())
                .note(req.getNote())
                .build());
        return PersonalListResponse.from(saved);
    }

    @Transactional
    public void removeFromWhitelist(String phoneNumber, String email) {
        User user = findUser(email);
        personalWhitelistRepository.deleteByUserIdAndPhoneNumber(user.getId(), phoneNumber);
    }

    public boolean isPersonallyWhitelisted(Long userId, String phone) {
        return personalWhitelistRepository.existsByUserIdAndPhoneNumber(userId, phone);
    }

    public List<PersonalListResponse> getMyBlacklist(String email) {
        User user = findUser(email);
        return personalBlacklistRepository.findByUserId(user.getId()).stream()
                .map(PersonalListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonalListResponse addToBlacklist(PersonalListRequest req, String email) {
        User user = findUser(email);
        if (personalBlacklistRepository.existsByUserIdAndPhoneNumber(user.getId(), req.getPhoneNumber())) {
            throw new DuplicateReportException("Número já está na blacklist pessoal");
        }
        PersonalBlacklist saved = personalBlacklistRepository.save(PersonalBlacklist.builder()
                .user(user)
                .phoneNumber(req.getPhoneNumber())
                .note(req.getNote())
                .build());
        return PersonalListResponse.from(saved);
    }

    @Transactional
    public void removeFromBlacklist(String phoneNumber, String email) {
        User user = findUser(email);
        personalBlacklistRepository.deleteByUserIdAndPhoneNumber(user.getId(), phoneNumber);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
