package com.blockendcall.service;

import com.blockendcall.dto.request.CreateApiKeyRequest;
import com.blockendcall.dto.response.ApiKeyResponse;
import com.blockendcall.entity.ApiKey;
import com.blockendcall.entity.User;
import com.blockendcall.exception.ResourceNotFoundException;
import com.blockendcall.repository.ApiKeyRepository;
import com.blockendcall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApiKeyResponse createApiKey(CreateApiKeyRequest req, String email) {
        User user = findUser(email);
        String keyValue = UUID.randomUUID().toString().replace("-", "") +
                          UUID.randomUUID().toString().replace("-", "");
        // Trim to 64 chars
        keyValue = keyValue.substring(0, 64);

        ApiKey apiKey = ApiKey.builder()
                .user(user)
                .keyValue(keyValue)
                .label(req.getLabel())
                .build();

        return ApiKeyResponse.from(apiKeyRepository.save(apiKey));
    }

    public List<ApiKeyResponse> getMyKeys(String email) {
        User user = findUser(email);
        return apiKeyRepository.findByUserId(user.getId()).stream()
                .map(ApiKeyResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeApiKey(Long keyId, String email) {
        User user = findUser(email);
        ApiKey apiKey = apiKeyRepository.findByIdAndUserId(keyId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("API key not found: " + keyId));
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
