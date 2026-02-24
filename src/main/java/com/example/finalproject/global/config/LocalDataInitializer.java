package com.example.finalproject.global.config;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.repository.ApprovalRepository;
import com.example.finalproject.coupon.domain.Coupon;
import com.example.finalproject.coupon.repository.CouponRepository;
import com.example.finalproject.order.domain.Cart;
import com.example.finalproject.order.domain.CartProduct;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.repository.CartProductRepository;
import com.example.finalproject.order.repository.CartRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.product.domain.ProductCategory;
import com.example.finalproject.product.repository.ProductCategoryRepository;
import com.example.finalproject.product.repository.ProductRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionProductRepository;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.payment.enums.PaymentMethodType;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.repository.PaymentMethodRepository;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.repository.AddressRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기존 시드 데이터 주입용. 이제 목데이터(Mock-Data-Loader SQL)로 통일하므로 기본 비활성화.
 * 필요 시 프로필에 local-initializer 를 추가하면 실행됨.
 * 운영 시 prod 추가
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class LocalDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoreCategoryRepository storeCategoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;
    private final ApprovalRepository approvalRepository;
    private final RiderRepository riderRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionProductRepository subscriptionProductRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // StoreCategory 시드 (입점 신청 시 카테고리 조회용)
        List<String> storeCategories = List.of("마트/슈퍼", "청과물", "정육점", "수산시장", "간식", "베이커리", "반찬가게", "철물/생활", "준비중");
        for (String categoryName : storeCategories) {
            if (storeCategoryRepository.findByCategoryName(categoryName).isEmpty()) {
                storeCategoryRepository.save(StoreCategory.builder().categoryName(categoryName).build());
                log.info("StoreCategory 시드: {}", categoryName);
            }
        }

        // Product Category 초기 데이터
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

        // CUSTOMER 역할 생성 (없으면) - 기본 회원 역할
        Role userRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("CUSTOMER")
                        .build()));

        roleRepository.findByRoleName("RIDER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("RIDER")
                        .build()));

        roleRepository.findByRoleName("STORE_OWNER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("STORE_OWNER")
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
    }

    private void seedCategory(String name, String iconUrl) {
        if (productCategoryRepository.findByCategoryName(name).isEmpty()) {
            productCategoryRepository.save(
                    ProductCategory.builder()
                            .categoryName(name)
                            .iconUrl(iconUrl)
                            .build()
            );
        }
    }
}
