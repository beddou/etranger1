package com.drag.foreignnationals.etranger.security;

import com.drag.foreignnationals.etranger.entity.RefreshToken;
import com.drag.foreignnationals.etranger.entity.User;
import com.drag.foreignnationals.etranger.repository.UserRepository;
import com.drag.foreignnationals.etranger.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService; // Add this
    private final UserRepository userRepository; // Add this
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Update constructor to include the new dependencies
    public JwtLoginFilter(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          RefreshTokenService refreshTokenService,
                          UserRepository userRepository) {
        this.setAuthenticationManager(authenticationManager);
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        if (!MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())) {
            throw new AuthenticationServiceException("Authentication content type not supported: " + request.getContentType());
        }

        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            if (loginRequest.username() == null || loginRequest.password() == null) {
                throw new AuthenticationServiceException("Username or Password missing in request");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

            setDetails(request, authToken);

            return this.getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        // 1. Generate Access Token
        String accessToken = jwtUtils.generateToken(userDetails);

        // 2. Generate Refresh Token (Matching your Controller logic)
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found post-auth"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 3. Build the response body
        Map<String, String> responseBody = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Write the full JSON (matches what your test expects)
        objectMapper.writeValue(response.getWriter(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        // Use 401 Unauthorized for login failures (more standard than 403 Forbidden)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String message = switch (failed.getClass().getSimpleName()) {
            case "LockedException" -> "User account is locked";
            case "DisabledException" -> "User account is disabled";
            case "BadCredentialsException" -> "Invalid username or password";
            default -> "Authentication failed";
        };

        // Standardized error JSON
        objectMapper.writeValue(response.getWriter(), Map.of("error", message));
    }

    private record LoginRequest(String username, String password) {}
    private record AuthResponse(String accessToken, String tokenType) {}
}