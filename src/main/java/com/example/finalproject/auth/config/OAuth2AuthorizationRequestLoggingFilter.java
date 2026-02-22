package com.example.finalproject.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

//실제 전송되는 redirect_uri KOE006 원인 확인
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OAuth2AuthorizationRequestLoggingFilter extends OncePerRequestFilter {

    private static final Pattern REDIRECT_URI_PARAM = Pattern.compile("redirect_uri=([^&]+)");

    private static void logIfKakaoRedirect(String location) {
        if (location == null || !location.contains("kauth.kakao.com")) return;
        Matcher m = REDIRECT_URI_PARAM.matcher(location);
        if (m.find()) {
            String encoded = m.group(1);
            String decoded = java.net.URLDecoder.decode(encoded, java.nio.charset.StandardCharsets.UTF_8);
            log.info("[카카오 인가 요청] redirect_uri(디코딩) = [{}] (카카오 콘솔과 1:1 일치해야 함)", decoded);
        }
        log.info("[카카오 인가 요청] 전체 Location = {}", location);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/oauth2/authorization/")) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void sendRedirect(String location) throws IOException {
                logIfKakaoRedirect(location);
                super.sendRedirect(location);
            }

            @Override
            public void setHeader(String name, String value) {
                if ("Location".equalsIgnoreCase(name)) logIfKakaoRedirect(value);
                super.setHeader(name, value);
            }

            @Override
            public void addHeader(String name, String value) {
                if ("Location".equalsIgnoreCase(name)) logIfKakaoRedirect(value);
                super.addHeader(name, value);
            }
        };
        filterChain.doFilter(request, wrapper);
    }
}
