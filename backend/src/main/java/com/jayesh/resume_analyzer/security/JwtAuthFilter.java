package com.jayesh.resume_analyzer.security;

import com.jayesh.resume_analyzer.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String where = request.getMethod() + " " + path;
        boolean publicPath = path.startsWith("/api/auth") || path.equals("/api/health") || path.equals("/error");

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            if (!publicPath) {
                log.warn("AUTH: no 'Authorization: Bearer ...' header on {}", where);
            }
        } else {
            String token = header.substring(7).trim();
            if (!jwtUtil.isValid(token)) {
                String start = token.length() > 15 ? token.substring(0, 15) : token;
                log.warn("AUTH: token is invalid or expired on {} (token starts with: {})", where, start);
            } else {
                String email = jwtUtil.extractEmail(token);
                userRepository.findByEmail(email).ifPresentOrElse(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }, () -> log.warn("AUTH: token is valid but user {} no longer exists in the database", email));
            }
        }

        chain.doFilter(request, response);
    }
}
