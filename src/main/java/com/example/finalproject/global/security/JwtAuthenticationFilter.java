package com.example.finalproject.global.security;

import com.example.finalproject.global.config.CookieUtil;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && !token.isBlank()) {
            if (jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseClaims(token);
                String subject = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                subject,
                                null,
                                roles.stream()
                                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                                        .toList()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    //Authorization: Bearer 헤더 또는 accessToken 쿠키에서 토큰 추출
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
