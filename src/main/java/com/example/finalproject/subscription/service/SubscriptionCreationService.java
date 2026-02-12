package com.example.finalproject.subscription.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.payment.repository.PaymentMethodRepository;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionDayOfWeek;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.domain.SubscriptionProductDayOfWeek;
import com.example.finalproject.subscription.dto.request.PostSubscriptionRequest;
import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionDayOfWeekRepository;
import com.example.finalproject.subscription.repository.SubscriptionProductDayOfWeekRepository;
import com.example.finalproject.subscription.repository.SubscriptionProductRepository;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.AddressRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionCreationService {


    private static final List<String> VALID_DELIVERY_TIME_SLOTS =
            List.of("08:00~11:00", "11:00~14:00", "14:00~17:00", "17:00~20:00");

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionProductRepository subscriptionProductRepository;
    private final SubscriptionProductDayOfWeekRepository subscriptionProductDayOfWeekRepository;
    private final SubscriptionDayOfWeekRepository subscriptionDayOfWeekRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public Subscription createPendingSubscription(PostSubscriptionRequest request, Long userId, User user) {
        SubscriptionProduct product = subscriptionProductRepository.findById(request.getSubscriptionProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_NOT_FOUND));
        if (product.getStatus() != SubscriptionProductStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_INVALID_STATUS);
        }

        Address address = addressRepository.findByIdWithUser(request.getAddressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!Objects.equals(address.getUser().getId(), userId)) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        PaymentMethod paymentMethod;
        if (request.getPaymentMethodId() != null) {
            paymentMethod = paymentMethodRepository.findByIdAndUser_Id(request.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        } else {
            paymentMethod = paymentMethodRepository.findFirstByUserIdAndIsDefaultTrue(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        }

        String deliveryTimeSlot = request.getDeliveryTimeSlot();
        if (deliveryTimeSlot == null || deliveryTimeSlot.isBlank()) {
            deliveryTimeSlot = VALID_DELIVERY_TIME_SLOTS.get(0);
        } else if (!VALID_DELIVERY_TIME_SLOTS.contains(deliveryTimeSlot)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_INVALID_DELIVERY_TIME_SLOT);
        }

        List<Short> daysOfWeek;
        if (request.getDeliveryDays() != null && !request.getDeliveryDays().isEmpty()) {
            daysOfWeek = request.getDeliveryDays().stream()
                    .map(Integer::shortValue)
                    .filter(d -> d >= 0 && d <= 6)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            List<SubscriptionProductDayOfWeek> productDays =
                    subscriptionProductDayOfWeekRepository.findBySubscriptionProductOrderById_DayOfWeekAsc(product);
            daysOfWeek = productDays.stream()
                    .map(d -> d.getId().getDayOfWeek())
                    .collect(Collectors.toList());
        }
        if (daysOfWeek.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDate nextPaymentDate = LocalDate.now().plusDays(SubscriptionProduct.SUBSCRIPTION_PERIOD_DAYS);

        Subscription subscription = Subscription.builder()
                .user(user)
                .store(product.getStore())
                .subscriptionProduct(product)
                .address(address)
                .paymentMethod(paymentMethod)
                .totalAmount(product.getPrice())
                .startedAt(startedAt)
                .nextPaymentDate(nextPaymentDate)
                .deliveryTimeSlot(deliveryTimeSlot)
                .status(SubscriptionStatus.PENDING)
                .build();
        subscription = subscriptionRepository.save(subscription);

        for (Short day : daysOfWeek) {
            subscriptionDayOfWeekRepository.save(
                    SubscriptionDayOfWeek.builder()
                            .subscription(subscription)
                            .dayOfWeek(day)
                            .build());
        }
        return subscription;
    }
}

