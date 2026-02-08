package com.example.finalproject.store.service;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionDayOfWeek;
import com.example.finalproject.subscription.domain.SubscriptionProductItem;
import com.example.finalproject.subscription.dto.response.GetDeliveryScheduleResponse;
import com.example.finalproject.subscription.dto.response.GetDeliveryScheduleResponse.DateDeliveryInfo;
import com.example.finalproject.subscription.dto.response.GetDeliveryScheduleResponse.ItemQuantity;
import com.example.finalproject.subscription.dto.response.GetDeliveryScheduleResponse.TimeSlotDeliveryInfo;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionDayOfWeekRepository;
import com.example.finalproject.subscription.repository.SubscriptionProductItemRepository;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마트 주간 배송 일정 서비스.
 * 3시간 단위 배송 시간대: 08:00~11:00, 11:00~14:00, 14:00~17:00, 17:00~20:00
 */
@Service
@RequiredArgsConstructor
public class StoreDeliveryScheduleService {

    private static final Set<SubscriptionStatus> DELIVERY_STATUSES = EnumSet.of(SubscriptionStatus.ACTIVE);
    private static final List<String> TIME_SLOTS = List.of(
            "08:00~11:00", "11:00~14:00", "14:00~17:00", "17:00~20:00"
    );
    private static final DateTimeFormatter DATE_LABEL_FORMAT =
            DateTimeFormatter.ofPattern("M월 d일 (E)", java.util.Locale.KOREAN);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDayOfWeekRepository subscriptionDayOfWeekRepository;
    private final SubscriptionProductItemRepository subscriptionProductItemRepository;

    /**
     * 마트의 주간 배송 일정을 조회한다.
     *
     * @param storeId   마트 ID
     * @param startDate 주 시작일 (월요일). null이면 이번 주 월요일
     * @return 주간 배송 일정
     */
    @Transactional(readOnly = true)
    public GetDeliveryScheduleResponse getDeliverySchedule(Long storeId, LocalDate startDate) {
        LocalDate weekStart = startDate != null ? startDate : getThisWeekMonday();
        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(weekStart.plusDays(i));
        }

        List<Subscription> allSubs = subscriptionRepository.findByStoreIdAndStatusIn(storeId, DELIVERY_STATUSES);
        Map<Long, Set<Short>> subDeliveryDays = loadSubscriptionDeliveryDays(allSubs);

        List<DateDeliveryInfo> dateDeliveries = new ArrayList<>();
        for (LocalDate date : weekDates) {
            short dayOfWeek = toDayOfWeek(date);
            DateDeliveryInfo info = buildDateDeliveryInfo(date, dayOfWeek, allSubs, subDeliveryDays);
            dateDeliveries.add(info);
        }

        return GetDeliveryScheduleResponse.builder()
                .weekStartDate(weekStart)
                .weekDates(weekDates)
                .dateDeliveries(dateDeliveries)
                .build();
    }

    private LocalDate getThisWeekMonday() {
        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();
        int diff = dow.getValue() - DayOfWeek.MONDAY.getValue();
        if (diff < 0) diff += 7;
        return today.minusDays(diff);
    }

    /** Java DayOfWeek (MON=1, SUN=7) -> DB day_of_week (0=일, 1=월, ..., 6=토) */
    private short toDayOfWeek(LocalDate date) {
        int v = date.getDayOfWeek().getValue();
        return (short) (v == 7 ? 0 : v);
    }

    private Map<Long, Set<Short>> loadSubscriptionDeliveryDays(List<Subscription> subs) {
        Map<Long, Set<Short>> map = new LinkedHashMap<>();
        for (Subscription sub : subs) {
            List<SubscriptionDayOfWeek> days = subscriptionDayOfWeekRepository.findBySubscription(sub);
            Set<Short> daySet = days.stream()
                    .map(d -> d.getId().getDayOfWeek())
                    .collect(Collectors.toSet());
            map.put(sub.getId(), daySet);
        }
        return map;
    }

    private DateDeliveryInfo buildDateDeliveryInfo(LocalDate date, short dayOfWeek,
                                                   List<Subscription> allSubs,
                                                   Map<Long, Set<Short>> subDeliveryDays) {
        List<Subscription> subsForDate = allSubs.stream()
                .filter(s -> subDeliveryDays.getOrDefault(s.getId(), Set.of()).contains(dayOfWeek))
                .filter(s -> s.getDeliveryTimeSlot() != null && TIME_SLOTS.contains(s.getDeliveryTimeSlot()))
                .toList();

        Map<String, List<Subscription>> bySlot = subsForDate.stream()
                .collect(Collectors.groupingBy(Subscription::getDeliveryTimeSlot));

        List<TimeSlotDeliveryInfo> timeSlots = new ArrayList<>();
        for (String slot : TIME_SLOTS) {
            List<Subscription> slotSubs = bySlot.getOrDefault(slot, List.of());
            List<ItemQuantity> items = aggregateItems(slotSubs);
            timeSlots.add(TimeSlotDeliveryInfo.builder()
                    .timeSlot(slot)
                    .deliveryCount(slotSubs.size())
                    .items(items)
                    .build());
        }

        String dateLabel = date.format(DATE_LABEL_FORMAT);
        return DateDeliveryInfo.builder()
                .date(date)
                .dateLabel(dateLabel)
                .timeSlots(timeSlots)
                .build();
    }

    private List<ItemQuantity> aggregateItems(List<Subscription> subs) {
        Map<String, Integer> productQty = new LinkedHashMap<>();
        for (Subscription sub : subs) {
            var items = subscriptionProductItemRepository.findBySubscriptionProductOrderById(sub.getSubscriptionProduct());
            for (SubscriptionProductItem item : items) {
                String name = item.getProduct().getProductName();
                int qty = (item.getQuantity() != null ? item.getQuantity() : 1);
                productQty.merge(name, qty, Integer::sum);
            }
        }
        return productQty.entrySet().stream()
                .map(e -> ItemQuantity.builder()
                        .productName(e.getKey())
                        .quantity(e.getValue())
                        .build())
                .toList();
    }
}
