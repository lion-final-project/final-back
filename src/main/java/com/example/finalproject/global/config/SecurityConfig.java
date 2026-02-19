package com.example.finalproject.global.config;

import com.example.finalproject.auth.config.KakaoProperties;
import com.example.finalproject.auth.config.OAuth2AuthorizationRequestLoggingFilter;
import com.example.finalproject.auth.config.OAuth2LoginSuccessHandler;
import com.example.finalproject.auth.service.KakaoService;
import com.example.finalproject.auth.social.SocialLoginStrategyRegistry;
import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.global.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

import java.util.function.Consumer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Profile(value = "!local")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({ JwtProperties.class, KakaoProperties.class })
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final SocialLoginStrategyRegistry socialLoginStrategyRegistry;
        private final ClientRegistrationRepository clientRegistrationRepository;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        @Lazy SocialLoginStrategyRegistry socialLoginStrategyRegistry,
                        ClientRegistrationRepository clientRegistrationRepository) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.socialLoginStrategyRegistry = socialLoginStrategyRegistry;
                this.clientRegistrationRepository = clientRegistrationRepository;
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
        public PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of( // 허용할 도메인 리스트
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:3000"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
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
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                                }))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(HttpMethod.GET, "/api/auth/check-email",
                                                                "/api/auth/check-phone")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/users/stores*").permitAll()
                                                .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/register",
                                                                "/api/auth/refresh", "/api/auth/login",
                                                                "/api/auth/social-signup/complete")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/send-verification",
                                                                "/api/auth/verify-phone")
                                                .permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers("/api/admin/notices/**").hasRole("ADMIN")
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/notices").permitAll()
                                                .requestMatchers("/api/riders", "/api/riders/register",
                                                                "/api/riders/approvals/*")
                                                .hasRole("CUSTOMER")
                                                .requestMatchers(HttpMethod.PATCH,"/api/riders/status").hasRole("RIDER")
                                                .requestMatchers(HttpMethod.POST,"/api/riders/locations").hasRole("RIDER")
                                                .requestMatchers(HttpMethod.DELETE,"/api/riders/locations/{riderId}").hasRole("RIDER")
                                                .requestMatchers(HttpMethod.GET,"/api/riders/locations/{riderId}").hasRole("RIDER")
                                                .requestMatchers("/api/riders/status").hasRole("RIDER")
                                                .requestMatchers(HttpMethod.GET, "/api/products/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/products/{productId}")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/stores/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/stores/*/products").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/users/stores").permitAll()
                                                .requestMatchers("/api/store/subscription-products",
                                                                "/api/store/subscription-products/**")
                                                .hasRole("STORE_OWNER")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(auth -> auth
                                                                .authorizationRequestResolver(
                                                                                kakaoAuthorizationRequestResolver()))
                                                .successHandler(new OAuth2LoginSuccessHandler(socialLoginStrategyRegistry)))
                                .addFilterBefore(new OAuth2AuthorizationRequestLoggingFilter(),
                                                OAuth2AuthorizationRequestRedirectFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}