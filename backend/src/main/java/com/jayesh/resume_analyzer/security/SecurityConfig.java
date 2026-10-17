package com.jayesh.resume_analyzer.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/jobs/*/match", "/api/jobs/*/apply").hasRole("CANDIDATE")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/recommended").hasRole("CANDIDATE")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/*/candidates").hasRole("RECRUITER")
                        .requestMatchers("/api/jobs/mine").hasAnyRole("RECRUITER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/jobs/**").authenticated()
                        .requestMatchers("/api/jobs/**").hasAnyRole("RECRUITER", "ADMIN")
                        .requestMatchers("/api/resumes/**", "/api/analyses/**").hasRole("CANDIDATE")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (req, res, e) -> res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}