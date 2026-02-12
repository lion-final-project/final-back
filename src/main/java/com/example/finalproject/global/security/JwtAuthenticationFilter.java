package com.example.finalproject.global.security;

import com.example.finalproject.global.config.CookieUtil;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && !token.isBlank() && jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String subject = claims.getSubject();
                User user = userRepository.findByEmail(subject).orElse(null);
                if (user != null && user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null) {
                    List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
                    Object rolesObj = claims.get("roles");
                    if (rolesObj instanceof List<?> rolesList) {
                        for (Object o : rolesList) {
                            String role = o == null ? "" : o.toString().trim();
                            if (!role.isEmpty()) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }
                        }
                    }
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(subject, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Throwable ex) {
            // 토큰 파싱/검증/roles 실패 시 인증만 하지 않고 진행
            log.debug("JWT 인증 스킵: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    //Authorization : Bearer 헤더 or AT 쿠키에서 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> CookieUtil.ACCESS_TOKEN_COOKIE.equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }
}
