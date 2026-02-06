package com.example.finalproject.global.config;

import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.order.domain.Cart;
import com.example.finalproject.order.domain.CartProduct;
import com.example.finalproject.order.repository.CartProductRepository;
import com.example.finalproject.order.repository.CartRepository;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.product.domain.ProductCategory;
import com.example.finalproject.product.repository.ProductCategoryRepository;
import com.example.finalproject.product.repository.ProductRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.StoreCategory;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.repository.StoreCategoryRepository;
import com.example.finalproject.store.repository.StoreRepository;
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
import java.util.List;

@Slf4j
@Component
@Profile("local")
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

        // 결제 더미데이터 (장바구니·주문서·결제 플로우 확인용)
        seedCheckoutDummyData(userRole);
    }

    // 결제 더미데이터
    /** 스토어, 상품, 테스트 유저 장바구니에 상품 담기 (장바구니·주문서·결제 플로우 확인용) */
    private void seedCheckoutDummyData(Role userRole) {
        String storeOwnerEmail = "storeowner@test.com";
        User storeOwner = userRepository.findByEmail(storeOwnerEmail).orElseGet(() -> {
            User owner = User.builder()
                    .email(storeOwnerEmail)
                    .password(passwordEncoder.encode("owner1234"))
                    .name("스토어오너")
                    .phone("01022222222")
                    .termsAgreed(true)
                    .privacyAgreed(true)
                    .termsAgreedAt(LocalDateTime.now())
                    .privacyAgreedAt(LocalDateTime.now())
                    .build();
            userRepository.save(owner);
            userRoleRepository.save(UserRole.builder().user(owner).role(userRole).build());
            log.info("결제 더미데이터: 스토어 오너 계정 생성 - {}", storeOwnerEmail);
            return owner;
        });

        Store store = storeRepository.findByOwner(storeOwner).orElseGet(() -> {
            StoreCategory martCategory = storeCategoryRepository.findByCategoryName("마트/슈퍼")
                    .orElseThrow();
            StoreAddress address = StoreAddress.builder()
                    .addressLine1("서울시 강남구 테스트로 123")
                    .addressLine2("1층")
                    .location(GeometryUtil.createPoint(127.0276, 37.4979))
                    .build();
            SettlementAccount settlement = SettlementAccount.builder()
                    .bankName("테스트은행")
                    .bankAccount("110-123-456789")
                    .accountHolder("스토어오너")
                    .build();
            SubmittedDocumentInfo doc = SubmittedDocumentInfo.builder()
                    .businessOwnerName("스토어오너")
                    .businessNumber("123456789012")
                    .telecomSalesReportNumber("제2024-서울강남-00001")
                    .build();
            Store newStore = Store.builder()
                    .owner(storeOwner)
                    .storeCategory(martCategory)
                    .storeName("동네마켓 테스트마트")
                    .phone("02-1234-5678")
                    .description("결제/장바구니 확인용 더미 스토어")
                    .representativeName("스토어오너")
                    .representativePhone("01022222222")
                    .submittedDocumentInfo(doc)
                    .address(address)
                    .settlementAccount(settlement)
                    .build();
            newStore = storeRepository.save(newStore);
            newStore.approve();
            log.info("결제 더미데이터: 스토어 생성 - {}", newStore.getStoreName());
            return newStore;
        });

        ProductCategory vegCategory = productCategoryRepository.findByCategoryName("채소")
                .orElseThrow();
        List<String[]> productRows = List.of(
                new String[]{"대파 1kg", "신선한 국내산 대파", "3000", "10", "0"},
                new String[]{"양파 2kg", "노란 양파", "4500", "20", "10"},
                new String[]{"당근 500g", "당근", "2500", "15", "0"}
        );
        List<Product> products = new java.util.ArrayList<>();
        for (String[] row : productRows) {
            String name = row[0];
            boolean exists = productRepository.findAll().stream()
                    .anyMatch(p -> name.equals(p.getProductName()) && p.getStore().getId().equals(store.getId()));
            if (exists) continue;
            Product p = Product.builder()
                    .store(store)
                    .productCategory(vegCategory)
                    .productName(name)
                    .description(row[1])
                    .price(Integer.parseInt(row[2]))
                    .stock(Integer.parseInt(row[3]))
                    .discountRate(row[4].isEmpty() ? null : Integer.parseInt(row[4]))
                    .build();
            p = productRepository.save(p);
            p.updateStatus(true);
            products.add(p);
        }
        if (!products.isEmpty()) {
            log.info("결제 더미데이터: 상품 {}건 생성", products.size());
        }

        User testUser = userRepository.findByEmail("user@test.com").orElse(null);
        if (testUser != null && !products.isEmpty()) {
            Cart cart = cartRepository.findByUserId(testUser.getId())
                    .orElseGet(() -> cartRepository.save(Cart.create(testUser)));
            for (Product product : products) {
                if (cartProductRepository.findByCartIdAndProductId(cart.getId(), product.getId()).isEmpty()) {
                    cartProductRepository.save(CartProduct.builder()
                            .cart(cart)
                            .product(product)
                            .store(store)
                            .quantity(2)
                            .build());
                }
            }
            log.info("결제 더미데이터: user@test.com 장바구니에 상품 {}건 담김 (수량 2)", products.size());
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
