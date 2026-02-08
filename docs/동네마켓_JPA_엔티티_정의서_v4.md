# 동네마켓_JPA_엔티티_정의서_v4.1

# 동네마켓 JPA 엔티티 정의서 v4.1

> 기술 스택: Spring Boot 3.x + JPA/Hibernate 6.x + Hibernate Spatial (PostGIS)
DBMS: PostgreSQL 16 + PostGIS 3.4
최종 수정일: 2026-01-30
설명: 동네마켓 플랫폼의 39개 테이블에 대한 JPA 엔티티 클래스 정의서입니다.
> 

---

## 1. 개요

### 1.1 기술 스택

| 항목 | 버전 |
| --- | --- |
| Spring Boot | 3.x |
| JPA / Hibernate | 6.x |
| Hibernate Spatial | 6.x (PostGIS 연동) |
| PostgreSQL | 16 |
| PostGIS | 3.4 |
| Java | 17+ |
| Lombok | 사용 |

### 1.2 패키지 구조

```
com.neighborhood.market
├── entity
│   ├── base/           # BaseEntity, BaseTimeEntity
│   ├── user/           # Role, UserRole, User, Address, SocialLogin
│   ├── store/          # Store, StoreBusinessHour
│   ├── product/        # Category, Product
│   ├── order/          # Order, StoreOrder, OrderProduct, Cart, CartProduct
│   ├── payment/        # Payment, PaymentRefund, PaymentMethod
│   ├── delivery/       # Rider, Delivery, RiderLocation, DeliveryPhoto
│   ├── subscription/   # SubscriptionProduct, Subscription, SubscriptionDayOfWeek,
│   │                   # SubscriptionProductItem, SubscriptionHistory
│   ├── review/         # Review
│   ├── settlement/     # Settlement, SettlementDetail
│   ├── approval/       # Approval, ApprovalDocument
│   └── common/         # Notification, NotificationBroadcast, Report, Inquiry,
│                       # Notice, Banner, Promotion, PromotionProduct
└── enums/              # 29개 ENUM 클래스
```

수정안: approval → moderation으로 수정, common을 세부 패키지로 나눔

```java
communication/
 ├── Notification
 ├── NotificationBroadcast
 ├── Inquiry

moderation/
 ├── Report
 ├── Approval
 ├── ApprovalDocument

content/
 ├── Notice
 ├── Banner
 ├── Promotion
 ├── PromotionProduct

```

### 1.3 공통 컨벤션

- **Lombok**: `@Getter`, `@NoArgsConstructor(access = PROTECTED)`, `@Builder`
- **Fetch 전략**: 모든 연관관계 `FetchType.LAZY` (N+1 방지)
- **PK 전략**: `@GeneratedValue(strategy = GenerationType.IDENTITY)` (PostgreSQL IDENTITY)
- **ENUM 매핑**: `@Enumerated(EnumType.STRING)` + `@Column(columnDefinition = "...")`
- **Soft Delete**: `@SQLRestriction("deleted_at IS NULL")` (Hibernate 6.3+)
- **PostGIS**: `org.locationtech.jts.geom.Point` + `@Column(columnDefinition = "GEOGRAPHY(POINT,4326)")`

---

## 2. 공통 정의

### 2.1 BaseTimeEntity

```java
package com.neighborhood.market.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 2.2 ENUM 클래스 (29개)

> 각 ENUM은 com.neighborhood.market.enums 패키지에 위치합니다.
> 

```java
// === 사용자 모듈 ===
public enum UserStatus { ACTIVE, INACTIVE, SUSPENDED, PENDING }
public enum SocialProvider { KAKAO, NAVER, GOOGLE, APPLE }

// === 마트 모듈 ===
public enum StoreStatus { PENDING, APPROVED, REJECTED, SUSPENDED }
public enum StoreActiveStatus { ACTIVE, INACTIVE, CLOSED }

// === 주문 모듈 ===
public enum OrderType { REGULAR, SUBSCRIPTION }
public enum OrderStatus { PENDING, PAID, PARTIAL_CANCELLED, CANCELLED, COMPLETED }
public enum StoreOrderStatus { PENDING, ACCEPTED, READY, PICKED_UP, DELIVERING, DELIVERED, CANCELLED, REJECTED }

// === 결제 모듈 ===
public enum PaymentMethodType { CARD, KAKAO_PAY, NAVER_PAY, TOSS_PAY }
public enum PaymentStatus { PENDING, COMPLETED, FAILED, CANCELLED, PARTIAL_REFUNDED, REFUNDED }

// === 배달 모듈 ===
public enum RiderOperationStatus { OFFLINE, ONLINE }
public enum RiderApprovalStatus { PENDING, APPROVED, REJECTED, SUSPENDED }
public enum DeliveryStatus { REQUESTED, ACCEPTED, PICKED_UP, DELIVERING, DELIVERED, CANCELLED }

// === 구독 모듈 ===
public enum SubscriptionProductStatus { ACTIVE, INACTIVE }
public enum SubscriptionStatus { ACTIVE, PAUSED, CANCELLATION_PENDING, CANCELLED }
public enum SubHistoryStatus { SCHEDULED, ORDERED, SKIPPED, COMPLETED }

// === 정산 모듈 ===
public enum SettlementTargetType { STORE, RIDER }
public enum SettlementStatus { PENDING, COMPLETED, FAILED }

// === 승인 모듈 ===
//NOTE: ApplicantType(STORE,RIDER)은 approval_documents용,
//       ApprovalApplicantType(MART,RIDER)은 approvals용. 향후 통합 권장.
public enum ApplicantType { STORE, RIDER }
public enum ApprovalApplicantType { MART, RIDER }
public enum ApprovalStatus { PENDING, APPROVED, REJECTED, HELD }
public enum DocumentType { BUSINESS_LICENSE, BUSINESS_REPORT, BANK_PASSBOOK, ID_CARD }

// === 기타 모듈 ===
public enum NotificationRefType { RIDER, STORE, CUSTOMER, ORDER, DELIVERY, PROMOTION }
public enum BroadcastRefType { RIDER, STORE, CUSTOMER, ALL }
public enum ReportTargetType { STORE, RIDER, CUSTOMER }
public enum ReportStatus { PENDING, RESOLVED }
public enum InquiryCategory { ORDER_PAYMENT, CANCELLATION_REFUND, DELIVERY, SERVICE, OTHER }
public enum InquiryStatus { PENDING, ANSWERED }
public enum ContentStatus { ACTIVE, INACTIVE }
public enum PromotionStatus { ACTIVE, ENDED }
```

### 2.3 PostGIS 설정 → 물어보기

**build.gradle 의존성**:

```groovy
implementation 'org.hibernate.orm:hibernate-spatial:6.x'
implementation 'org.locationtech.jts:jts-core:1.19.0'
```

**application.yml**:

```yaml
spring:
jpa:
database-platform: org.hibernate.spatial.dialect.postgis.PostgisPG10Dialect
properties:
hibernate:
dialect: org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. 모듈별 엔티티 정의 (39개)

---

## 3.1 사용자 모듈

### Role

```java
package com.neighborhood.market.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    @Builder
    public Role(String roleName) {
        this.roleName = roleName;
    }
}
```

---

### UserRole

```java
package com.neighborhood.market.entity.user;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_roles",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_user_roles_user_role",
           columnNames = {"user_id", "role_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    private User user;

    // FK → roles (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_user_roles_role"))
    private Role role;

    @Builder
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }
}
```

---

### User

```java
package com.neighborhood.market.entity.user;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "user_status DEFAULT 'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean termsAgreed = false;

    @Column(nullable = false)
    private Boolean privacyAgreed = false;

    private LocalDateTime termsAgreedAt;

    private LocalDateTime privacyAgreedAt;

    private LocalDateTime deletedAt;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialLogin> socialLogins = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }
}
```

---

### Address

```java
package com.neighborhood.market.entity.user;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "addresses",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_addresses_user_address",
                             columnNames = {"user_id", "address_line1", "address_line2"}),
           @UniqueConstraint(name = "uq_addresses_user_name",
                             columnNames = {"user_id", "address_name"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_addresses_user"))
    private User user;

    @Column(nullable = false, length = 20)
    private String contact;

    @Column(name = "address_name", nullable = false, length = 50)
    private String addressName;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    // PostGIS GEOGRAPHY(POINT, 4326)
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Builder
    public Address(User user, String contact, String addressName,
                   String postalCode, String addressLine1, String addressLine2,
                   Point location, Boolean isDefault) {
        this.user = user;
        this.contact = contact;
        this.addressName = addressName;
        this.postalCode = postalCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.location = location;
        this.isDefault = isDefault != null ? isDefault : false;
    }
}
```

---

### SocialLogin

```java
package com.neighborhood.market.entity.user;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.SocialProvider;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_logins",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_social_provider_user",
           columnNames = {"provider", "provider_user_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialLogin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_social_logins_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "social_provider")
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    private LocalDateTime deletedAt;

    @Builder
    public SocialLogin(User user, SocialProvider provider,
                       String providerUserId, LocalDateTime connectedAt) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.connectedAt = connectedAt;
    }
}
```

---

## 3.2 마트 모듈

### Store

```java
package com.neighborhood.market.entity.store;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.StoreActiveStatus;
import com.neighborhood.market.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE RESTRICT), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_stores_owner"))
    private User owner;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "business_number", nullable = false, unique = true, length = 12)
    private String businessNumber;

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName;

    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;

    @Column(length = 20)
    private String phone;

    @Column(name = "telecom_sales_report_number", length = 50)
    private String telecomSalesReportNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    // PostGIS GEOGRAPHY(POINT, 4326)
    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(name = "settlement_bank_name", nullable = false, length = 50)
    private String settlementBankName;

    @Column(name = "settlement_bank_account", nullable = false, length = 255)
    private String settlementBankAccount;

    @Column(name = "settlement_account_holder", nullable = false, length = 50)
    private String settlementAccountHolder;

    @Column(name = "store_image", length = 500)
    private String storeImage;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "store_status DEFAULT 'PENDING'")
    private StoreStatus status = StoreStatus.PENDING;

    @Column(name = "is_delivery_available", nullable = false)
    private Boolean isDeliveryAvailable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false, columnDefinition = "store_active_status DEFAULT 'INACTIVE'")
    private StoreActiveStatus isActive = StoreActiveStatus.INACTIVE;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("5.00");

    private LocalDateTime deletedAt;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreBusinessHour> businessHours = new ArrayList<>();

    @Builder
    public Store(User owner, String storeName, String businessNumber,
                 String representativeName, String representativePhone,
                 String addressLine1, String postalCode,
                 String settlementBankName, String settlementBankAccount,
                 String settlementAccountHolder) {
        this.owner = owner;
        this.storeName = storeName;
        this.businessNumber = businessNumber;
        this.representativeName = representativeName;
        this.representativePhone = representativePhone;
        this.addressLine1 = addressLine1;
        this.postalCode = postalCode;
        this.settlementBankName = settlementBankName;
        this.settlementBankAccount = settlementBankAccount;
        this.settlementAccountHolder = settlementAccountHolder;
    }
}
```

---

### StoreBusinessHour

```java
package com.neighborhood.market.entity.store;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "store_business_hours",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_business_hours_store_day",
           columnNames = {"store_id", "day_of_week"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreBusinessHour extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → stores (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_business_hours_store"))
    private Store store;

    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Builder
    public StoreBusinessHour(Store store, Short dayOfWeek,
                             LocalTime openTime, LocalTime closeTime, Boolean isClosed) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed != null ? isClosed : false;
    }
}
```

---

## 3.3 상품 모듈

### Category

```java
package com.neighborhood.market.entity.product;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    private String categoryName;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Builder
    public Category(String categoryName, String iconUrl) {
        this.categoryName = categoryName;
        this.iconUrl = iconUrl;
    }
}
```

---

### Product

```java
package com.neighborhood.market.entity.product;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.store.Store;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → stores (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_products_store"))
    private Store store;

    // FK → categories (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 30)
    private String unit;

    @Column(length = 100)
    private String origin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Builder
    public Product(Store store, Category category, String productName,
                   BigDecimal price, Integer stock) {
        this.store = store;
        this.category = category;
        this.productName = productName;
        this.price = price;
        this.stock = stock != null ? stock : 0;
    }
}
```

---

## 3.4 주문 모듈

### Order

```java
package com.neighborhood.market.entity.order;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.OrderStatus;
import com.neighborhood.market.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    // FK → users (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, columnDefinition = "order_type DEFAULT 'REGULAR'")
    private OrderType orderType = OrderType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "order_status DEFAULT 'PENDING'")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_product_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProductPrice;

    @Column(name = "total_delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDeliveryFee = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    // PostGIS GEOGRAPHY(POINT, 4326)
    @Column(name = "delivery_location", columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point deliveryLocation;

    @Column(name = "delivery_request", length = 255)
    private String deliveryRequest;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "order")
    private List<StoreOrder> storeOrders = new ArrayList<>();

    @Builder
    public Order(String orderNumber, User user, OrderType orderType,
                 BigDecimal totalProductPrice, BigDecimal totalDeliveryFee,
                 BigDecimal finalPrice, String deliveryAddress,
                 Point deliveryLocation, LocalDateTime orderedAt) {
        this.orderNumber = orderNumber;
        this.user = user;
        this.orderType = orderType != null ? orderType : OrderType.REGULAR;
        this.totalProductPrice = totalProductPrice;
        this.totalDeliveryFee = totalDeliveryFee != null ? totalDeliveryFee : BigDecimal.ZERO;
        this.finalPrice = finalPrice;
        this.deliveryAddress = deliveryAddress;
        this.deliveryLocation = deliveryLocation;
        this.orderedAt = orderedAt;
    }
}
```

---

### StoreOrder

```java
package com.neighborhood.market.entity.order;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.store.Store;
import com.neighborhood.market.enums.OrderType;
import com.neighborhood.market.enums.StoreOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → orders (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_store_orders_order"))
    private Order order;

    // FK → stores (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_store_orders_store"))
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, columnDefinition = "order_type DEFAULT 'REGULAR'")
    private OrderType orderType = OrderType.REGULAR;

    @Column(name = "prep_time")
    private Integer prepTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "store_order_status DEFAULT 'PENDING'")
    private StoreOrderStatus status = StoreOrderStatus.PENDING;

    @Column(name = "store_product_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal storeProductPrice;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "final_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    private LocalDateTime acceptedAt;
    private LocalDateTime preparedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "storeOrder")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Builder
    public StoreOrder(Order order, Store store, OrderType orderType,
                      BigDecimal storeProductPrice, BigDecimal deliveryFee,
                      BigDecimal finalPrice) {
        this.order = order;
        this.store = store;
        this.orderType = orderType != null ? orderType : OrderType.REGULAR;
        this.storeProductPrice = storeProductPrice;
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.finalPrice = finalPrice;
    }
}
```

---

### OrderProduct

```java
package com.neighborhood.market.entity.order;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → store_orders (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_products_store_order"))
    private StoreOrder storeOrder;

    // FK → products (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_order_products_product"))
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public OrderProduct(StoreOrder storeOrder, Product product,
                        String productNameSnapshot, BigDecimal priceSnapshot,
                        Integer quantity) {
        this.storeOrder = storeOrder;
        this.product = product;
        this.productNameSnapshot = productNameSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
    }
}
```

---

### Cart

```java
package com.neighborhood.market.entity.order;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_carts_user"))
    private User user;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartProduct> cartProducts = new ArrayList<>();

    @Builder
    public Cart(User user) {
        this.user = user;
    }
}
```

---

### CartProduct

```java
package com.neighborhood.market.entity.order;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.product.Product;
import com.neighborhood.market.entity.store.Store;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_products",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_cart_products_cart_product",
           columnNames = {"cart_id", "product_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → carts (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cart_products_cart"))
    private Cart cart;

    // FK → products (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cart_products_product"))
    private Product product;

    // FK → stores (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cart_products_store"))
    private Store store;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public CartProduct(Cart cart, Product product, Store store, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.store = store;
        this.quantity = quantity;
    }
}
```

---

## 3.5 결제 모듈

### Payment

```java
package com.neighborhood.market.entity.payment;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.Order;
import com.neighborhood.market.enums.PaymentMethodType;
import com.neighborhood.market.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → orders (ON DELETE RESTRICT), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_payments_order"))
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, columnDefinition = "payment_method_type")
    private PaymentMethodType paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, columnDefinition = "payment_status DEFAULT 'PENDING'")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "pg_provider", length = 50)
    private String pgProvider;

    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 30)
    private String cardNumberMasked;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    private LocalDateTime paidAt;

    @Builder
    public Payment(Order order, PaymentMethodType paymentMethod, BigDecimal amount) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
}
```

---

### PaymentRefund

```java
package com.neighborhood.market.entity.payment;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → payments (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private Payment payment;

    // FK → store_orders (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_refunds_store_order"))
    private StoreOrder storeOrder;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    private LocalDateTime refundedAt;

    @Builder
    public PaymentRefund(Payment payment, StoreOrder storeOrder,
                         BigDecimal refundAmount, String refundReason) {
        this.payment = payment;
        this.storeOrder = storeOrder;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
    }
}
```

---

### PaymentMethod

```java
package com.neighborhood.market.entity.payment;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_methods",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_payment_methods_billing",
           columnNames = {"user_id", "billing_key"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_payment_methods_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, columnDefinition = "payment_method_type")
    private PaymentMethodType methodType;

    @Column(name = "billing_key", nullable = false, length = 255)
    private String billingKey;

    @Column(name = "card_company", length = 50)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 30)
    private String cardNumberMasked;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Builder
    public PaymentMethod(User user, PaymentMethodType methodType,
                         String billingKey, Boolean isDefault) {
        this.user = user;
        this.methodType = methodType;
        this.billingKey = billingKey;
        this.isDefault = isDefault != null ? isDefault : false;
    }
}
```

---

## 3.6 배달 모듈

### Rider

```java
package com.neighborhood.market.entity.delivery;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.RiderApprovalStatus;
import com.neighborhood.market.enums.RiderOperationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE RESTRICT), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_riders_user"))
    private User user;

    @Column(name = "id_card_verified", nullable = false)
    private Boolean idCardVerified = false;

    @Column(name = "id_card_image", length = 500)
    private String idCardImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status", nullable = false, columnDefinition = "rider_operation_status DEFAULT 'OFFLINE'")
    private RiderOperationStatus operationStatus = RiderOperationStatus.OFFLINE;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "rider_approval_status DEFAULT 'PENDING'")
    private RiderApprovalStatus status = RiderApprovalStatus.PENDING;

    @Builder
    public Rider(User user) {
        this.user = user;
    }
}
```

---

### Delivery

```java
package com.neighborhood.market.entity.delivery;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import com.neighborhood.market.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → store_orders (ON DELETE RESTRICT), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_deliveries_store_order"))
    private StoreOrder storeOrder;

    // FK → riders (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_deliveries_rider"))
    private Rider rider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "delivery_status DEFAULT 'REQUESTED'")
    private DeliveryStatus status = DeliveryStatus.REQUESTED;

    @Column(name = "delivery_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "rider_earning", nullable = false, precision = 12, scale = 2)
    private BigDecimal riderEarning;

    @Column(name = "distance_km", precision = 5, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Builder
    public Delivery(StoreOrder storeOrder, Rider rider,
                    BigDecimal deliveryFee, BigDecimal riderEarning,
                    LocalDateTime requestedAt) {
        this.storeOrder = storeOrder;
        this.rider = rider;
        this.deliveryFee = deliveryFee;
        this.riderEarning = riderEarning;
        this.requestedAt = requestedAt;
    }
}
```

---

### RiderLocation

```java
package com.neighborhood.market.entity.delivery;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiderLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → riders (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_rider_locations_rider"))
    private Rider rider;

    // FK → deliveries (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id",
                foreignKey = @ForeignKey(name = "fk_rider_locations_delivery"))
    private Delivery delivery;

    // PostGIS GEOGRAPHY(POINT, 4326)
    @Column(nullable = false, columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(precision = 6, scale = 2)
    private BigDecimal accuracy;

    @Column(precision = 5, scale = 2)
    private BigDecimal speed;

    @Column(precision = 5, scale = 2)
    private BigDecimal heading;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public RiderLocation(Rider rider, Delivery delivery, Point location,
                         LocalDateTime recordedAt) {
        this.rider = rider;
        this.delivery = delivery;
        this.location = location;
        this.recordedAt = recordedAt;
    }
}
```

---

### DeliveryPhoto

```java
package com.neighborhood.market.entity.delivery;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → deliveries (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_delivery_photos_delivery"))
    private Delivery delivery;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime deletedAt;

    @Builder
    public DeliveryPhoto(Delivery delivery, String photoUrl, LocalDateTime expiresAt) {
        this.delivery = delivery;
        this.photoUrl = photoUrl;
        this.expiresAt = expiresAt;
    }
}
```

---

## 3.7 구독 모듈

### SubscriptionProduct

```java
package com.neighborhood.market.entity.subscription;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.store.Store;
import com.neighborhood.market.enums.SubscriptionProductStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscription_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → stores (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_sub_products_store"))
    private Store store;

    @Column(name = "subscription_product_name", nullable = false, length = 200)
    private String subscriptionProductName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "total_delivery_count", nullable = false)
    private Integer totalDeliveryCount;

    @Column(name = "delivery_count_of_week", nullable = false)
    private Integer deliveryCountOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_product_status DEFAULT 'ACTIVE'")
    private SubscriptionProductStatus status = SubscriptionProductStatus.ACTIVE;

    @Column(name = "subscription_url", length = 500)
    private String subscriptionUrl;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "subscriptionProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionProductItem> items = new ArrayList<>();

    @Builder
    public SubscriptionProduct(Store store, String subscriptionProductName,
                               BigDecimal price, Integer totalDeliveryCount,
                               Integer deliveryCountOfWeek) {
        this.store = store;
        this.subscriptionProductName = subscriptionProductName;
        this.price = price;
        this.totalDeliveryCount = totalDeliveryCount;
        this.deliveryCountOfWeek = deliveryCountOfWeek;
    }
}
```

---

### Subscription

```java
package com.neighborhood.market.entity.subscription;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.payment.PaymentMethod;
import com.neighborhood.market.entity.store.Store;
import com.neighborhood.market.entity.user.Address;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subscriptions_user"))
    private User user;

    // FK → stores (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subscriptions_store"))
    private Store store;

    // FK → subscription_products (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subscriptions_sub_product"))
    private SubscriptionProduct subscriptionProduct;

    @Column(name = "delivery_time_slot", length = 30)
    private String deliveryTimeSlot;

    // FK → addresses (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subscriptions_address"))
    private Address address;

    // FK → payment_methods (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_subscriptions_payment_method"))
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_status DEFAULT 'ACTIVE'")
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "cycle_count", nullable = false)
    private Integer cycleCount = 1;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubscriptionDayOfWeek> dayOfWeeks = new ArrayList<>();

    @OneToMany(mappedBy = "subscription")
    private List<SubscriptionHistory> histories = new ArrayList<>();

    @Builder
    public Subscription(User user, Store store, SubscriptionProduct subscriptionProduct,
                        Address address, PaymentMethod paymentMethod,
                        BigDecimal totalAmount, LocalDateTime startedAt) {
        this.user = user;
        this.store = store;
        this.subscriptionProduct = subscriptionProduct;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.startedAt = startedAt;
    }
}
```

---

### SubscriptionDayOfWeekId (복합 PK)

```java
package com.neighborhood.market.entity.subscription;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SubscriptionDayOfWeekId implements Serializable {

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "day_of_week")
    private Short dayOfWeek;

    public SubscriptionDayOfWeekId(Long subscriptionId, Short dayOfWeek) {
        this.subscriptionId = subscriptionId;
        this.dayOfWeek = dayOfWeek;
    }
}
```

### SubscriptionDayOfWeek

```java
package com.neighborhood.market.entity.subscription;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_day_of_week")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionDayOfWeek extends BaseTimeEntity {

    @EmbeddedId
    private SubscriptionDayOfWeekId id;

    // FK → subscriptions (ON DELETE CASCADE)
    @MapsId("subscriptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id",
                foreignKey = @ForeignKey(name = "fk_sub_day_subscription"))
    private Subscription subscription;

    @Builder
    public SubscriptionDayOfWeek(Subscription subscription, Short dayOfWeek) {
        this.id = new SubscriptionDayOfWeekId(subscription.getId(), dayOfWeek);
        this.subscription = subscription;
    }
}
```

---

### SubscriptionProductItem

```java
package com.neighborhood.market.entity.subscription;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_product_items",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_sub_items_product",
           columnNames = {"subscription_product_id", "product_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionProductItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → subscription_products (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_sub_items_sub_product"))
    private SubscriptionProduct subscriptionProduct;

    // FK → products (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_sub_items_product"))
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public SubscriptionProductItem(SubscriptionProduct subscriptionProduct,
                                   Product product, Integer quantity) {
        this.subscriptionProduct = subscriptionProduct;
        this.product = product;
        this.quantity = quantity;
    }
}
```

---

### SubscriptionHistory

```java
package com.neighborhood.market.entity.subscription;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import com.neighborhood.market.enums.SubHistoryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscription_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → subscriptions (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_sub_history_subscription"))
    private Subscription subscription;

    @Column(name = "cycle_count", nullable = false)
    private Integer cycleCount;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "sub_history_status DEFAULT 'SCHEDULED'")
    private SubHistoryStatus status = SubHistoryStatus.SCHEDULED;

    // FK → store_orders (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id",
                foreignKey = @ForeignKey(name = "fk_sub_history_store_order"))
    private StoreOrder storeOrder;

    @Builder
    public SubscriptionHistory(Subscription subscription, Integer cycleCount,
                               LocalDate scheduledDate) {
        this.subscription = subscription;
        this.cycleCount = cycleCount;
        this.scheduledDate = scheduledDate;
    }
}
```

---

## 3.8 리뷰 모듈

### Review

```java
package com.neighborhood.market.entity.review;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import com.neighborhood.market.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → store_orders (ON DELETE RESTRICT), 1:1
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_reviews_store_order"))
    private StoreOrder storeOrder;

    // FK → users (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_reviews_user"))
    private User user;

    @Column(nullable = false)
    private Short rating;

    @Column(length = 100)
    private String content;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Builder
    public Review(StoreOrder storeOrder, User user, Short rating, String content) {
        this.storeOrder = storeOrder;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }
}
```

---

## 3.9 정산 모듈

### Settlement

```java
package com.neighborhood.market.entity.settlement;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.SettlementStatus;
import com.neighborhood.market.enums.SettlementTargetType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "settlement_target_type")
    private SettlementTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "settlement_period_start", nullable = false)
    private LocalDate settlementPeriodStart;

    @Column(name = "settlement_period_end", nullable = false)
    private LocalDate settlementPeriodEnd;

    @Column(name = "total_sales", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSales;

    @Column(name = "platform_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "settlement_status DEFAULT 'PENDING'")
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    private LocalDateTime settledAt;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SettlementDetail> details = new ArrayList<>();

    @Builder
    public Settlement(SettlementTargetType targetType, Long targetId,
                      LocalDate settlementPeriodStart, LocalDate settlementPeriodEnd,
                      BigDecimal totalSales, BigDecimal platformFee,
                      BigDecimal settlementAmount) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.settlementPeriodStart = settlementPeriodStart;
        this.settlementPeriodEnd = settlementPeriodEnd;
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.settlementAmount = settlementAmount;
    }
}
```

---

### SettlementDetail

```java
package com.neighborhood.market.entity.settlement;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "settlement_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → settlements (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_settlement_details_settlement"))
    private Settlement settlement;

    // FK → store_orders (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_settlement_details_store_order"))
    private StoreOrder storeOrder;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Builder
    public SettlementDetail(Settlement settlement, StoreOrder storeOrder,
                            BigDecimal amount, BigDecimal fee, BigDecimal netAmount) {
        this.settlement = settlement;
        this.storeOrder = storeOrder;
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount;
    }
}
```

---

## 3.10 승인 관리 모듈

### Approval

```java
package com.neighborhood.market.entity.approval;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.ApprovalApplicantType;
import com.neighborhood.market.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approvals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Approval extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE RESTRICT) — 신청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_approvals_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false, columnDefinition = "approval_applicant_type")
    private ApprovalApplicantType applicantType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "approval_status DEFAULT 'PENDING'")
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String reason;

    // FK → users (ON DELETE SET NULL) — 처리 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by",
                foreignKey = @ForeignKey(name = "fk_approvals_approved_by"))
    private User approvedBy;

    private LocalDateTime approvedAt;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "approval", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalDocument> documents = new ArrayList<>();

    @Builder
    public Approval(User user, ApprovalApplicantType applicantType) {
        this.user = user;
        this.applicantType = applicantType;
    }
}
```

---

### ApprovalDocument

```java
package com.neighborhood.market.entity.approval;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.ApplicantType;
import com.neighborhood.market.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_documents",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_approval_docs_type",
           columnNames = {"approval_id", "document_type"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false, columnDefinition = "applicant_type")
    private ApplicantType applicantType;

    // FK → approvals (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_approval_docs_approval"))
    private Approval approval;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, columnDefinition = "document_type")
    private DocumentType documentType;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Builder
    public ApprovalDocument(ApplicantType applicantType, Approval approval,
                            DocumentType documentType, String documentUrl) {
        this.applicantType = applicantType;
        this.approval = approval;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
    }
}
```

---

## 3.11 기타 모듈

### Notification

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.NotificationRefType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", columnDefinition = "notification_ref_type")
    private NotificationRefType referenceType;

    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Builder
    public Notification(User user, String title, String content,
                        NotificationRefType referenceType) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.referenceType = referenceType;
    }
}
```

---

### NotificationBroadcast

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.BroadcastRefType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_broadcasts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationBroadcast extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false,
            columnDefinition = "broadcast_ref_type DEFAULT 'ALL'")
    private BroadcastRefType referenceType = BroadcastRefType.ALL;

    @Builder
    public NotificationBroadcast(String title, String content,
                                 BroadcastRefType referenceType) {
        this.title = title;
        this.content = content;
        this.referenceType = referenceType != null ? referenceType : BroadcastRefType.ALL;
    }
}
```

---

### Report

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.order.StoreOrder;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.ReportStatus;
import com.neighborhood.market.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → store_orders (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id",
                foreignKey = @ForeignKey(name = "fk_reports_store_order"))
    private StoreOrder storeOrder;

    // FK → users (ON DELETE RESTRICT) — 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_reports_reporter"))
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporter_type", nullable = false, columnDefinition = "report_target_type")
    private ReportTargetType reporterType;

    // FK → users (ON DELETE RESTRICT) — 피신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_reports_target"))
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "report_target_type")
    private ReportTargetType targetType;

    @Column(name = "reason_detail", nullable = false, columnDefinition = "TEXT")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "report_status DEFAULT 'PENDING'")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "report_result", columnDefinition = "TEXT")
    private String reportResult;

    private LocalDateTime resolvedAt;

    @Builder
    public Report(StoreOrder storeOrder, User reporter, ReportTargetType reporterType,
                  User target, ReportTargetType targetType, String reasonDetail) {
        this.storeOrder = storeOrder;
        this.reporter = reporter;
        this.reporterType = reporterType;
        this.target = target;
        this.targetType = targetType;
        this.reasonDetail = reasonDetail;
    }
}
```

---

### Inquiry

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.InquiryCategory;
import com.neighborhood.market.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → users (ON DELETE RESTRICT) — 문의자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_inquiries_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "inquiry_category")
    private InquiryCategory category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "inquiry_status DEFAULT 'PENDING'")
    private InquiryStatus status = InquiryStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private LocalDateTime answeredAt;

    // FK → users (ON DELETE SET NULL) — 답변 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answered_by",
                foreignKey = @ForeignKey(name = "fk_inquiries_answered_by"))
    private User answeredBy;

    @Builder
    public Inquiry(User user, InquiryCategory category, String title, String content) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
    }
}
```

---

### Notice

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.user.User;
import com.neighborhood.market.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // FK → users (ON DELETE RESTRICT) — 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_notices_author"))
    private User author;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "content_status DEFAULT 'ACTIVE'")
    private ContentStatus status = ContentStatus.ACTIVE;

    @Builder
    public Notice(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
}
```

---

### Banner

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "background_color", length = 50)
    private String backgroundColor;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "content_status DEFAULT 'ACTIVE'")
    private ContentStatus status = ContentStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Builder
    public Banner(String title, String imageUrl, LocalDateTime startedAt,
                  LocalDateTime endedAt) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }
}
```

---

### Promotion

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "promotion_status DEFAULT 'ACTIVE'")
    private PromotionStatus status = PromotionStatus.ACTIVE;

    // === 연관관계 (역방향) ===

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionProduct> promotionProducts = new ArrayList<>();

    @Builder
    public Promotion(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
```

---

### PromotionProduct

```java
package com.neighborhood.market.entity.common;

import com.neighborhood.market.entity.base.BaseTimeEntity;
import com.neighborhood.market.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotion_products",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_promo_products_unique",
           columnNames = {"promotion_id", "product_id"}
       ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → promotions (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_promo_products_promotion"))
    private Promotion promotion;

    // FK → products (ON DELETE RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_promo_products_product"))
    private Product product;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public PromotionProduct(Promotion promotion, Product product, Integer sortOrder) {
        this.promotion = promotion;
        this.product = product;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}
```

---

## 4. 엔티티 요약

| 모듈 | 엔티티 수 | 클래스명 |
| --- | --- | --- |
| 3.1 사용자 | 5 | Role, UserRole, User, Address, SocialLogin |
| 3.2 마트 | 2 | Store, StoreBusinessHour |
| 3.3 상품 | 2 | Category, Product |
| 3.4 주문 | 5 | Order, StoreOrder, OrderProduct, Cart, CartProduct |
| 3.5 결제 | 3 | Payment, PaymentRefund, PaymentMethod |
| 3.6 배달 | 4 | Rider, Delivery, RiderLocation, DeliveryPhoto |
| 3.7 구독 | 5+1 | SubscriptionProduct, Subscription, SubscriptionDayOfWeek (+Id), SubscriptionProductItem, SubscriptionHistory |
| 3.8 리뷰 | 1 | Review |
| 3.9 정산 | 2 | Settlement, SettlementDetail |
| 3.10 승인 | 2 | Approval, ApprovalDocument |
| 3.11 기타 | 8 | Notification, NotificationBroadcast, Report, Inquiry, Notice, Banner, Promotion, PromotionProduct |
| **합계** | **39** | + SubscriptionDayOfWeekId (복합키 클래스) |

---

## 변경 이력

| 버전 | 일자 | 작성자 | 변경 내용 |
| --- | --- | --- | --- |
| v4.1 | 2026-01-30 | Backend Dev | JPA 엔티티 정의서 초판 작성 (ERD v4.1 기반) |