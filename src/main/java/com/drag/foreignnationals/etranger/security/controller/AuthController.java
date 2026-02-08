package com.drag.foreignnationals.etranger.security.controller;

import com.drag.foreignnationals.etranger.security.entity.RefreshToken;
import com.drag.foreignnationals.etranger.security.entity.User;
import com.drag.foreignnationals.etranger.security.repository.RefreshTokenRepository;
import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.drag.foreignnationals.etranger.security.model.CustomUserDetails;
import com.drag.foreignnationals.etranger.security.service.CustomUserDetailsService;
import com.drag.foreignnationals.etranger.security.jwt.JwtUtils;
import com.drag.foreignnationals.etranger.security.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED
                        , "Invalid refresh token"));

        User user = refreshToken.getUser();

        CustomUserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String newAccessToken = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }

    @PostMapping("/logout")
    @Transactional
    public void logout(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        refreshTokenRepository.deleteByUser(user);
    }

    public record RefreshTokenRequest(String refreshToken) {}
}
