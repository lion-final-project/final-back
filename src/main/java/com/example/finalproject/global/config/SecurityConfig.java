package com.example.finalproject.global.config;

import com.example.finalproject.auth.config.KakaoProperties;
import com.example.finalproject.auth.config.NaverProperties;
import com.example.finalproject.auth.config.OAuth2AuthorizationRequestLoggingFilter;
import com.example.finalproject.auth.config.OAuth2LoginSuccessHandler;
import com.example.finalproject.auth.service.AuthService;
import com.example.finalproject.auth.social.SocialLoginStrategyRegistry;
import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Profile(value = "!local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({ JwtProperties.class, KakaoProperties.class, NaverProperties.class })
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final SocialLoginStrategyRegistry socialLoginStrategyRegistry;
        private final ClientRegistrationRepository clientRegistrationRepository;
        private final AuthService authService;
        private final JwtProperties jwtProperties;
        private final JwtTokenProvider jwtTokenProvider;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        SocialLoginStrategyRegistry socialLoginStrategyRegistry,
                        ClientRegistrationRepository clientRegistrationRepository,
                        AuthService authService,
                        JwtProperties jwtProperties,
                        JwtTokenProvider jwtTokenProvider) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.socialLoginStrategyRegistry = socialLoginStrategyRegistry;
                this.clientRegistrationRepository = clientRegistrationRepository;
                this.authService = authService;
                this.jwtProperties = jwtProperties;
                this.jwtTokenProvider = jwtTokenProvider;
        }

        private OAuth2AuthorizationRequestResolver kakaoAuthorizationRequestResolver() {
                DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
                                clientRegistrationRepository, "/oauth2/authorization");
                resolver.setAuthorizationRequestCustomizer(kakaoPromptLoginCustomizer());
                return resolver;
        }

        private Consumer<OAuth2AuthorizationRequest.Builder> kakaoPromptLoginCustomizer() {
                return customizer -> customizer
                                .additionalParameters(params -> params.put("prompt", "login"));
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOriginPatterns(List.of( // 허용할 도메인 리스트 (와일드카드 지원)
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:3000",
                                "http://43.200.37.106",
                                "http://43.200.37.106:8080",
                                "https://*.vercel.app")); // Vercel 배포용
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
                config.setAllowCredentials(true); // 쿠키 포함 여부

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .sessionManagement(
                                                session -> session.sessionCreationPolicy(
                                                                SessionCreationPolicy.IF_REQUIRED)) // OAuth는 세션 사용
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, CookieUtil.clearAccessTokenCookie().toString());
                                                        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, CookieUtil.clearRefreshTokenCookie().toString());
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                                }))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/auth/check-email",
                                                                "/api/auth/check-phone",
                                                                "/api/auth/clear-cookies",
                                                                "/api/users/stores*",
                                                                "/api/products/categories",
                                                                "/api/products/{productId}",
                                                                "/api/stores/categories",
                                                                "/api/stores/*/products",
                                                                "/api/users/stores")
                                                .permitAll()
                                                .requestMatchers(req -> "GET".equals(req.getMethod())
                                                                && req.getRequestURI().matches(".*/api/stores/[0-9]+$"))
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/auth/register",
                                                                "/api/auth/refresh",
                                                                "/api/auth/login",
                                                                "/api/auth/logout",
                                                                "/api/auth/social-signup/complete",
                                                                "/api/auth/password-reset/request",
                                                                "/api/auth/password-reset/confirm",
                                                                "/api/auth/send-verification",
                                                                "/api/auth/verify-phone")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/oauth2/authorization/**",
                                                                "/login/oauth2/code/**",
                                                                "/error",
                                                                "/api/notices")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/api/admin/notices/**",
                                                                "/api/admin/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(
                                                                "/api/riders/register",
                                                                "/api/riders/approvals/*")
                                                .hasRole("CUSTOMER")
                                                // ── RIDER (GET) ──
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/riders",
                                                                "/api/riders/locations/{riderId}",
                                                                "/api/riders/deliveries/**")
                                                .hasRole("RIDER")
                                                // ── RIDER (POST) ──
                                                .requestMatchers(HttpMethod.POST,
                                                                "/api/riders/locations",
                                                                "/api/riders/deliveries/*/accept",
                                                                "/api/storage/delivery/**")
                                                .hasRole("RIDER")
                                                // ── RIDER (PATCH) ──
                                                .requestMatchers(HttpMethod.PATCH,
                                                                "/api/riders/status",
                                                                "/api/riders/deliveries/*/pickup",
                                                                "/api/riders/deliveries/*/start",
                                                                "/api/riders/deliveries/*/complete")
                                                .hasRole("RIDER")
                                                // ── RIDER (DELETE) ──
                                                .requestMatchers(HttpMethod.DELETE,
                                                                "/api/riders/locations/{riderId}")
                                                .hasRole("RIDER")
                                                .requestMatchers(
                                                                "/api/store/orders/**",
                                                                "/api/store/settlements",
                                                                "/api/store/settlements/**",
                                                                "/api/store/subscriptions",
                                                                "/api/store/subscriptions/**",
                                                                "/api/store/subscription-products",
                                                                "/api/store/subscription-products/**")
                                                .hasRole("STORE")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(auth -> auth
                                                                .authorizationRequestResolver(
                                                                                kakaoAuthorizationRequestResolver()))
                                                .successHandler(new OAuth2LoginSuccessHandler(
                                                                socialLoginStrategyRegistry,
                                                                authService,
                                                                jwtProperties,
                                                                jwtTokenProvider)))
                                .addFilterBefore(new OAuth2AuthorizationRequestLoggingFilter(),
                                                OAuth2AuthorizationRequestRedirectFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
