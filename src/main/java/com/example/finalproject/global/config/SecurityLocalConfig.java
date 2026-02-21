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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Profile(value = "local")
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({ JwtProperties.class, KakaoProperties.class, NaverProperties.class })
public class SecurityLocalConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final SocialLoginStrategyRegistry socialLoginStrategyRegistry;
        private final ClientRegistrationRepository clientRegistrationRepository;
        private final AuthService authService;
        private final JwtProperties jwtProperties;
        private final JwtTokenProvider jwtTokenProvider;

        public SecurityLocalConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        @Lazy SocialLoginStrategyRegistry socialLoginStrategyRegistry,
                        ClientRegistrationRepository clientRegistrationRepository,
                        @Lazy AuthService authService,
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
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                                }))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/admin/**").authenticated()
                                                .requestMatchers("/api/cart", "/api/cart/**").authenticated()
                                                .requestMatchers("/api/checkout", "/api/checkout/**").authenticated()
                                                .requestMatchers("/api/orders", "/api/orders/**").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/products/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/products/{productId}")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/stores/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/stores/*/products").permitAll()
                                                .requestMatchers(req -> "GET".equals(req.getMethod())
                                                                && req.getRequestURI().matches(".*/api/stores/[0-9]+$"))
                                                .permitAll()
                                                .requestMatchers("/api/products/**").authenticated()
                                                .requestMatchers("/api/stores/**", "/api/store/**").authenticated()
                                                .requestMatchers("/api/storage/store/image").authenticated()
                                                .requestMatchers("/api/storage/product/image*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/users/stores*").permitAll()
                                                .anyRequest().permitAll())
                                .oauth2Login(oauth2 -> {
                                        oauth2.authorizationEndpoint(auth -> auth
                                                        .authorizationRequestResolver(
                                                                        kakaoAuthorizationRequestResolver()));
                                        oauth2.successHandler(new OAuth2LoginSuccessHandler(
                                                        socialLoginStrategyRegistry, authService, jwtProperties,
                                                        jwtTokenProvider));
                                })
                                .addFilterBefore(new OAuth2AuthorizationRequestLoggingFilter(),
                                                OAuth2AuthorizationRequestRedirectFilter.class)
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }
}
