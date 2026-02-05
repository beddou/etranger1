package com.drag.foreignnationals.etranger.service.impl;

import com.drag.foreignnationals.etranger.entity.RefreshToken;
import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.repository.RefreshTokenRepository;
import com.drag.foreignnationals.etranger.repository.UserRepository;
import com.drag.foreignnationals.etranger.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;

    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000; // 7 days



    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // One refresh token per user
        repository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        return repository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        return token;
    }
}
