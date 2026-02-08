package com.drag.foreignnationals.etranger.security;

import com.drag.foreignnationals.etranger.security.repository.UserRepository;
import com.drag.foreignnationals.etranger.security.filter.guard.JwtAuthFilter;
import com.drag.foreignnationals.etranger.security.jwt.JwtUtils;
import com.drag.foreignnationals.etranger.security.filter.login.JwtLoginFilter;
import com.drag.foreignnationals.etranger.security.service.CustomUserDetailsService;
import com.drag.foreignnationals.etranger.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Allows @PreAuthorize and @PostAuthorize for fine-grained auditing
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter; // The "Security Guard" (OncePerRequestFilter)
    private final JwtUtils jwtUtils;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthFilter jwtAuthFilter,
                          JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager,
                                           RefreshTokenService refreshTokenService, // Inject here
                                           UserRepository userRepository) throws Exception { // Inject here

        // Pass all 4 dependencies to the constructor
        JwtLoginFilter loginFilter = new JwtLoginFilter(
                authManager,
                jwtUtils,
                refreshTokenService,
                userRepository
        );
        loginFilter.setFilterProcessesUrl("/auth/login");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // --- ADD THIS SECTION ---
                .exceptionHandling(exception -> exception
                        // 1. Handle Unauthenticated (No/Bad Token) -> 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}");
                        })
                        // 2. Handle Unauthorized (Wrong Role) -> 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"errorCode\":\"ACCESS_DENIED\",\"message\":\"Insufficient permissions\"}");
                        })
                )
                // ------------------------
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/logout","/auth/refresh", "/error").permitAll()
                        //.requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilter(loginFilter);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5. Explicitly link UserDetailsService to a DaoAuthenticationProvider
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}