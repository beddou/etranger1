package com.drag.foreignnationals.etranger.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils; // your JWT utility class


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        // 1. Check if the request is actually JSON
        if (!"application/json".equals(request.getContentType())) {
            throw new AuthenticationServiceException("Authentication content type not supported: " + request.getContentType());
        }

        try {
            // 2. Use a dedicated DTO instead of a generic Map
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            // 3. Basic validation (Fail fast)
            if (loginRequest.username() == null || loginRequest.password() == null) {
                throw new AuthenticationServiceException("Username or Password missing in request");
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

            // 4. Optional: Set details so you can track IP address/Session ID later
            setDetails(request, authToken);

            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            // 5. Throw a proper Spring Security exception
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }

    // Use a Record for a clean, immutable DTO
    private record LoginRequest(String username, String password) {}

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        // 1. Generate the token
        String token = jwtUtils.generateToken((UserDetails) authResult.getPrincipal());

        // 2. Prepare the response body as an object (or Record)
        AuthResponse authResponse = new AuthResponse(token, "Bearer");

        // 3. Set proper headers
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Optional: Add token to header as well (common practice)
        response.setHeader("Authorization", "Bearer " + token);

        // 4. Use ObjectMapper to write the JSON safely
        new ObjectMapper().writeValue(response.getWriter(), authResponse);
    }

    // Immutable DTO for the response
    private record AuthResponse(String accessToken, String tokenType) {}

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String message;
        if (failed instanceof LockedException) {
            message = "User account is locked";
        } else if (failed instanceof BadCredentialsException) {
            message = "Invalid username or password";
        } else {
            message = "Authentication failed";
        }

        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
