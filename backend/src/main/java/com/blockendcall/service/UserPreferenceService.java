package com.blockendcall.service;

import com.blockendcall.dto.request.UpdatePreferencesRequest;
import com.blockendcall.dto.response.UserPreferenceResponse;
import com.blockendcall.entity.User;
import com.blockendcall.entity.UserPreference;
import com.blockendcall.repository.UserPreferenceRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserPreferenceResponse getPreferences(String email) {
        User user = findUser(email);
        UserPreference pref = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserPreference newPref = UserPreference.builder()
                            .user(user)
                            .build();
                    return userPreferenceRepository.save(newPref);
                });
        return UserPreferenceResponse.from(pref);
    }

    @Transactional
    public UserPreferenceResponse updatePreferences(UpdatePreferencesRequest req, String email) {
        User user = findUser(email);
        UserPreference pref = userPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> UserPreference.builder().user(user).build());

        if (req.getBlockOnlyConfirmed() != null) {
            pref.setBlockOnlyConfirmed(req.getBlockOnlyConfirmed());
        }
        if (req.getNotifyOnConfirm() != null) {
            pref.setNotifyOnConfirm(req.getNotifyOnConfirm());
        }
        if (req.getSensitivity() != null) {
            pref.setSensitivity(req.getSensitivity());
        }
        if (req.getParanoiaMode() != null) {
            pref.setParanoiaMode(req.getParanoiaMode());
        }
        if (req.getBlockTelemarketing() != null) {
            pref.setBlockTelemarketing(req.getBlockTelemarketing());
        }
        if (req.getBlockScam() != null) {
            pref.setBlockScam(req.getBlockScam());
        }
        if (req.getBlockRobocall() != null) {
            pref.setBlockRobocall(req.getBlockRobocall());
        }
        if (req.getBlockSilent() != null) {
            pref.setBlockSilent(req.getBlockSilent());
        }
        if (req.getVoicemailMode() != null) {
            pref.setVoicemailMode(req.getVoicemailMode());
        }

        pref.setUpdatedAt(LocalDateTime.now());
        return UserPreferenceResponse.from(userPreferenceRepository.save(pref));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
