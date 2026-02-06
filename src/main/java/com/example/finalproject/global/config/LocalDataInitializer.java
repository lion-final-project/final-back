package com.example.finalproject.global.config;

import com.example.finalproject.product.domain.ProductCategory;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.enums.StoreCategoryType;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.product.repository.CategoryRepository;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // StoreCategory 시드 (입점 신청 시 카테고리 조회용)
        for (StoreCategoryType type : StoreCategoryType.values()) {
            if (storeCategoryRepository.findByCategoryName(type).isEmpty()) {
                storeCategoryRepository.save(StoreCategory.builder().categoryName(type).build());
                log.info("StoreCategory 시드: {}", type);
            }
        }

        // Product ProductCategory 초기 데이터
        seedCategory("채소", "https://cdn.example.com/icons/vegetable.png");
        seedCategory("과일", "https://cdn.example.com/icons/fruit.png");
        seedCategory("정육", "https://cdn.example.com/icons/meat.png");
        seedCategory("유제품", "https://cdn.example.com/icons/dairy.png");
        seedCategory("식재료", "https://cdn.example.com/icons/ingredient.png");
        seedCategory("생활용품", "https://cdn.example.com/icons/living.png");

        // ADMIN 역할 생성 (없으면)
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ADMIN")
                        .build()));

        // USER 역할 생성 (없으면)
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("USER")
                        .build()));

        // ADMIN 테스트 계정 생성 (없으면)
        String adminEmail = "admin@test.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin1234"))
                    .name("관리자")
                    .phone("01000000000")
                    .termsAgreed(true)
                    .privacyAgreed(true)
                    .termsAgreedAt(LocalDateTime.now())
                    .privacyAgreedAt(LocalDateTime.now())
                    .build();
            userRepository.save(adminUser);

            userRoleRepository.save(UserRole.builder()
                    .user(adminUser)
                    .role(adminRole)
                    .build());

            log.info("==============================================");
            log.info("ADMIN 테스트 계정이 생성되었습니다.");
            log.info("Email: {}", adminEmail);
            log.info("Password: admin1234");
            log.info("==============================================");
        }

        // 일반 USER 테스트 계정 생성 (없으면)
        String userEmail = "user@test.com";
        if (userRepository.findByEmail(userEmail).isEmpty()) {
            User normalUser = User.builder()
                    .email(userEmail)
                    .password(passwordEncoder.encode("user1234"))
                    .name("테스트유저")
                    .phone("01011111111")
                    .termsAgreed(true)
                    .privacyAgreed(true)
                    .termsAgreedAt(LocalDateTime.now())
                    .privacyAgreedAt(LocalDateTime.now())
                    .build();
            userRepository.save(normalUser);

            userRoleRepository.save(UserRole.builder()
                    .user(normalUser)
                    .role(userRole)
                    .build());

            log.info("==============================================");
            log.info("USER 테스트 계정이 생성되었습니다.");
            log.info("Email: {}", userEmail);
            log.info("Password: user1234");
            log.info("==============================================");
        }
    }

    private void seedCategory(String name, String iconUrl) {
        if (categoryRepository.findByCategoryName(name).isEmpty()) {
            categoryRepository.save(
                    ProductCategory.builder()
                            .categoryName(name)
                            .iconUrl(iconUrl)
                            .build()
            );
        }
    }
}
