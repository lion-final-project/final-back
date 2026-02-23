package com.example.finalproject.admin.service.finance;

import com.example.finalproject.admin.dto.finance.settlement.*;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.rider.batch.RiderSettlementBatchLauncher;
import com.example.finalproject.settlement.store.batch.StoreSettlementBatchLauncher;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFinanceSettlementService {

    private final AdminFinanceCommonSupport common;
    private final SettlementRepository settlementRepository;
    private final StoreRepository storeRepository;
    private final RiderRepository riderRepository;
    private final StoreSettlementBatchLauncher storeSettlementBatchLauncher;
    private final RiderSettlementBatchLauncher riderSettlementBatchLauncher;

    public AdminStoreSettlementSummaryResponse getStoreSettlementSummary(String adminEmail, String yearMonth) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        List<Settlement> settlements = settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.STORE, range.startDate(), range.endDate());
        return buildStoreSummary(settlements);
    }

    public AdminStoreSettlementTrendResponse getStoreSettlementTrend(String adminEmail, Integer months, String yearMonth) {
        common.validateAdmin(adminEmail);
        int targetMonths = (months == null || months < 2 || months > 12) ? 6 : months;
        YearMonth endMonth = (yearMonth == null || yearMonth.isBlank()) ? YearMonth.now() : common.parseYearMonth(yearMonth);
        YearMonth startMonth = endMonth.minusMonths(targetMonths - 1L);

        List<String> labels = new ArrayList<>();
        Map<YearMonth, Long> amountMap = new LinkedHashMap<>();
        for (int i = 0; i < targetMonths; i++) {
            YearMonth month = startMonth.plusMonths(i);
            labels.add(month.getMonthValue() + "월");
            amountMap.put(month, 0L);
        }

        List<Settlement> settlements = settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.STORE, startMonth.atDay(1), endMonth.atEndOfMonth());

        for (Settlement settlement : settlements) {
            YearMonth month = YearMonth.from(settlement.getSettlementPeriodStart());
            if (!amountMap.containsKey(month)) continue;
            long amount = settlement.getSettlementAmount() == null ? 0L : settlement.getSettlementAmount();
            amountMap.compute(month, (k, v) -> v == null ? amount : v + amount);
        }

        List<Long> yValues = amountMap.values().stream().toList();
        long totalAmount = yValues.stream().mapToLong(Long::longValue).sum();
        long first = yValues.isEmpty() ? 0L : yValues.get(0);
        long last = yValues.isEmpty() ? 0L : yValues.get(yValues.size() - 1);
        double changeRate = first == 0L ? 0.0 : (double) (last - first) / first * 100.0;

        return AdminStoreSettlementTrendResponse.builder()
                .xLabels(labels)
                .yValues(yValues)
                .totalAmount(totalAmount)
                .changeRate(Math.round(changeRate * 10.0) / 10.0)
                .build();
    }

    public AdminStoreSettlementListResponse getStoreSettlements(String adminEmail, String yearMonth, SettlementStatus status, String keyword, Pageable pageable) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        String normalizedKeyword = common.normalizeKeyword(keyword);

        Page<Settlement> settlementPage;
        List<Settlement> settlementsForStats = (status == null)
                ? settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.STORE, range.startDate(), range.endDate())
                : settlementRepository.findByTargetTypeAndStatusAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.STORE, status, range.startDate(), range.endDate());

        if (normalizedKeyword.isBlank()) {
            settlementPage = (status == null)
                    ? settlementRepository.findByTargetTypeAndSettlementPeriodStartBetween(
                    SettlementTargetType.STORE, range.startDate(), range.endDate(), pageable)
                    : settlementRepository.findByTargetTypeAndStatusAndSettlementPeriodStartBetween(
                    SettlementTargetType.STORE, status, range.startDate(), range.endDate(), pageable);
        } else {
            List<Settlement> baseList = settlementsForStats;
            Map<Long, Store> storeMap = buildStoreMap(baseList);
            List<Settlement> filtered = baseList.stream().filter(settlement -> {
                Store store = storeMap.get(settlement.getTargetId());
                if (store == null) return false;
                String storeName = store.getStoreName() == null ? "" : store.getStoreName();
                String storeIdCode = common.toStoreIdCode(store.getId());
                return storeName.toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || storeIdCode.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
            }).toList();
            settlementPage = toPage(filtered, pageable);
        }

        Map<Long, Store> storeMap = buildStoreMap(settlementPage.getContent());
        List<AdminStoreSettlementListResponse.Item> items = settlementPage.getContent().stream().map(settlement -> {
            Store store = storeMap.get(settlement.getTargetId());
            return AdminStoreSettlementListResponse.Item.builder()
                    .settlementId(settlement.getId())
                    .storeId(store != null ? store.getId() : settlement.getTargetId())
                    .storeName(store != null ? store.getStoreName() : "알수없는 마트")
                    .idCode(common.toStoreIdCode(settlement.getTargetId()))
                    .region(store != null ? common.extractRegion(store) : "미상")
                    .amount(settlement.getSettlementAmount() != null ? settlement.getSettlementAmount() : 0L)
                    .settlementPeriodStart(settlement.getSettlementPeriodStart())
                    .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                    .settledAt(settlement.getSettledAt())
                    .status(settlement.getStatus())
                    .statusLabel(common.toSettlementStatusLabel(settlement.getStatus()))
                    .build();
        }).toList();

        long completed = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.COMPLETED).count();
        long pending = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.PENDING).count();
        long failed = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.FAILED).count();

        return AdminStoreSettlementListResponse.builder()
                .content(items)
                .stats(AdminStoreSettlementListResponse.Stats.builder()
                        .total(settlementsForStats.size())
                        .completed(completed)
                        .pending(pending)
                        .failed(failed)
                        .build())
                .page(AdminStoreSettlementListResponse.PageInfo.builder()
                        .page(settlementPage.getNumber())
                        .size(settlementPage.getSize())
                        .totalElements(settlementPage.getTotalElements())
                        .totalPages(settlementPage.getTotalPages())
                        .hasNext(settlementPage.hasNext())
                        .build())
                .build();
    }

    public AdminRiderSettlementSummaryResponse getRiderSettlementSummary(String adminEmail, String yearMonth) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        List<Settlement> settlements = settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.RIDER, range.startDate(), range.endDate());
        return buildRiderSummary(settlements);
    }

    public AdminRiderSettlementTrendResponse getRiderSettlementTrend(String adminEmail, Integer months, String yearMonth) {
        common.validateAdmin(adminEmail);
        int targetMonths = (months == null || months < 2 || months > 12) ? 6 : months;
        YearMonth endMonth = (yearMonth == null || yearMonth.isBlank()) ? YearMonth.now() : common.parseYearMonth(yearMonth);
        YearMonth startMonth = endMonth.minusMonths(targetMonths - 1L);

        List<String> labels = new ArrayList<>();
        Map<YearMonth, Long> amountMap = new LinkedHashMap<>();
        for (int i = 0; i < targetMonths; i++) {
            YearMonth month = startMonth.plusMonths(i);
            labels.add(month.getMonthValue() + "월");
            amountMap.put(month, 0L);
        }

        List<Settlement> settlements = settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.RIDER, startMonth.atDay(1), endMonth.atEndOfMonth());

        for (Settlement settlement : settlements) {
            YearMonth month = YearMonth.from(settlement.getSettlementPeriodStart());
            if (!amountMap.containsKey(month)) continue;
            long amount = settlement.getSettlementAmount() == null ? 0L : settlement.getSettlementAmount();
            amountMap.compute(month, (k, v) -> v == null ? amount : v + amount);
        }

        List<Long> yValues = amountMap.values().stream().toList();
        long totalAmount = yValues.stream().mapToLong(Long::longValue).sum();
        long first = yValues.isEmpty() ? 0L : yValues.get(0);
        long last = yValues.isEmpty() ? 0L : yValues.get(yValues.size() - 1);
        double changeRate = first == 0L ? 0.0 : (double) (last - first) / first * 100.0;

        return AdminRiderSettlementTrendResponse.builder()
                .xLabels(labels)
                .yValues(yValues)
                .totalAmount(totalAmount)
                .changeRate(Math.round(changeRate * 10.0) / 10.0)
                .build();
    }

    public AdminRiderSettlementListResponse getRiderSettlements(String adminEmail, String yearMonth, SettlementStatus status, String keyword, Pageable pageable) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        String normalizedKeyword = common.normalizeKeyword(keyword);

        Page<Settlement> settlementPage;
        List<Settlement> settlementsForStats = (status == null)
                ? settlementRepository.findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.RIDER, range.startDate(), range.endDate())
                : settlementRepository.findByTargetTypeAndStatusAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                SettlementTargetType.RIDER, status, range.startDate(), range.endDate());

        if (normalizedKeyword.isBlank()) {
            settlementPage = (status == null)
                    ? settlementRepository.findByTargetTypeAndSettlementPeriodStartBetween(
                    SettlementTargetType.RIDER, range.startDate(), range.endDate(), pageable)
                    : settlementRepository.findByTargetTypeAndStatusAndSettlementPeriodStartBetween(
                    SettlementTargetType.RIDER, status, range.startDate(), range.endDate(), pageable);
        } else {
            List<Settlement> baseList = settlementsForStats;
            Map<Long, Rider> riderMap = buildRiderMap(baseList);
            List<Settlement> filtered = baseList.stream().filter(settlement -> {
                Rider rider = riderMap.get(settlement.getTargetId());
                if (rider == null) return false;
                String riderName = rider.getDisplayName() == null ? "" : rider.getDisplayName();
                String riderIdCode = common.toRiderIdCode(rider.getId());
                return riderName.toLowerCase(Locale.ROOT).contains(normalizedKeyword)
                        || riderIdCode.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
            }).toList();
            settlementPage = toPage(filtered, pageable);
        }

        Map<Long, Rider> riderMap = buildRiderMap(settlementPage.getContent());
        List<AdminRiderSettlementListResponse.Item> items = settlementPage.getContent().stream().map(settlement -> {
            Rider rider = riderMap.get(settlement.getTargetId());
            return AdminRiderSettlementListResponse.Item.builder()
                    .settlementId(settlement.getId())
                    .riderId(rider != null ? rider.getId() : settlement.getTargetId())
                    .riderName(rider != null ? rider.getDisplayName() : "알수없는 라이더")
                    .riderPhone(rider != null ? rider.getDisplayPhone() : "-")
                    .idCode(common.toRiderIdCode(settlement.getTargetId()))
                    .region("전국")
                    .amount(settlement.getSettlementAmount() != null ? settlement.getSettlementAmount() : 0L)
                    .settlementPeriodStart(settlement.getSettlementPeriodStart())
                    .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                    .settledAt(settlement.getSettledAt())
                    .status(settlement.getStatus())
                    .statusLabel(common.toSettlementStatusLabel(settlement.getStatus()))
                    .build();
        }).toList();

        long completed = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.COMPLETED).count();
        long pending = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.PENDING).count();
        long failed = settlementsForStats.stream().filter(item -> item.getStatus() == SettlementStatus.FAILED).count();

        return AdminRiderSettlementListResponse.builder()
                .content(items)
                .stats(AdminRiderSettlementListResponse.Stats.builder()
                        .total(settlementsForStats.size())
                        .completed(completed)
                        .pending(pending)
                        .failed(failed)
                        .build())
                .page(AdminRiderSettlementListResponse.PageInfo.builder()
                        .page(settlementPage.getNumber())
                        .size(settlementPage.getSize())
                        .totalElements(settlementPage.getTotalElements())
                        .totalPages(settlementPage.getTotalPages())
                        .hasNext(settlementPage.hasNext())
                        .build())
                .build();
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeStoreSettlement(String adminEmail, AdminStoreSettlementExecuteRequest request) {
        common.validateAdmin(adminEmail);
        YearMonth target = common.parseYearMonth(request.getYearMonth());
        int completedCount = storeSettlementBatchLauncher.runMonthlyPipeline(target);
        return AdminStoreSettlementExecuteResponse.builder().yearMonth(target.toString()).completedCount(completedCount).build();
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeStoreSettlementSingle(String adminEmail, Long settlementId) {
        common.validateAdmin(adminEmail);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산 정보를 찾을 수 없습니다."));
        if (settlement.getTargetType() != SettlementTargetType.STORE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "마트 정산 ID가 아닙니다.");
        }
        if (settlement.getStatus() != SettlementStatus.COMPLETED) {
            settlement.complete(LocalDateTime.now());
        }
        return AdminStoreSettlementExecuteResponse.builder()
                .yearMonth(YearMonth.from(settlement.getSettlementPeriodStart()).toString())
                .completedCount(1)
                .build();
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeRiderSettlement(String adminEmail, AdminStoreSettlementExecuteRequest request) {
        common.validateAdmin(adminEmail);
        YearMonth target = common.parseYearMonth(request.getYearMonth());
        long before = settlementRepository
                .findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                        SettlementTargetType.RIDER, target.atDay(1), target.atEndOfMonth())
                .size();
        riderSettlementBatchLauncher.runMonthlyPipeline(target);
        long after = settlementRepository
                .findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                        SettlementTargetType.RIDER, target.atDay(1), target.atEndOfMonth())
                .size();
        int completedCount = (int) Math.max(0L, after - before);
        return AdminStoreSettlementExecuteResponse.builder().yearMonth(target.toString()).completedCount(completedCount).build();
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeRiderSettlementSingle(String adminEmail, Long settlementId) {
        common.validateAdmin(adminEmail);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산 정보를 찾을 수 없습니다."));
        if (settlement.getTargetType() != SettlementTargetType.RIDER) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "라이더 정산 ID가 아닙니다.");
        }
        if (settlement.getStatus() != SettlementStatus.COMPLETED) {
            settlement.complete(LocalDateTime.now());
        }
        return AdminStoreSettlementExecuteResponse.builder()
                .yearMonth(YearMonth.from(settlement.getSettlementPeriodStart()).toString())
                .completedCount(1)
                .build();
    }

    private AdminStoreSettlementSummaryResponse buildStoreSummary(List<Settlement> settlements) {
        long totalTargets = settlements.size();
        long completedTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.COMPLETED).count();
        long pendingTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.PENDING).count();
        long failedTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.FAILED).count();
        long totalSettlementAmount = settlements.stream()
                .map(Settlement::getSettlementAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        double completedRate = totalTargets == 0 ? 0.0 : (double) completedTargets * 100.0 / totalTargets;
        return AdminStoreSettlementSummaryResponse.builder()
                .totalTargets(totalTargets)
                .completedTargets(completedTargets)
                .pendingTargets(pendingTargets)
                .failedTargets(failedTargets)
                .totalSettlementAmount(totalSettlementAmount)
                .completedRate(Math.round(completedRate * 10.0) / 10.0)
                .build();
    }

    private AdminRiderSettlementSummaryResponse buildRiderSummary(List<Settlement> settlements) {
        long totalTargets = settlements.size();
        long completedTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.COMPLETED).count();
        long pendingTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.PENDING).count();
        long failedTargets = settlements.stream().filter(item -> item.getStatus() == SettlementStatus.FAILED).count();
        long totalSettlementAmount = settlements.stream()
                .map(Settlement::getSettlementAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        double completedRate = totalTargets == 0 ? 0.0 : (double) completedTargets * 100.0 / totalTargets;
        return AdminRiderSettlementSummaryResponse.builder()
                .totalTargets(totalTargets)
                .completedTargets(completedTargets)
                .pendingTargets(pendingTargets)
                .failedTargets(failedTargets)
                .totalSettlementAmount(totalSettlementAmount)
                .completedRate(Math.round(completedRate * 10.0) / 10.0)
                .build();
    }

    private Map<Long, Store> buildStoreMap(Collection<Settlement> settlements) {
        List<Long> storeIds = settlements.stream().map(Settlement::getTargetId).distinct().toList();
        Map<Long, Store> map = new HashMap<>();
        for (Store store : storeRepository.findAllById(storeIds)) {
            map.put(store.getId(), store);
        }
        return map;
    }

    private Map<Long, Rider> buildRiderMap(Collection<Settlement> settlements) {
        List<Long> riderIds = settlements.stream().map(Settlement::getTargetId).distinct().toList();
        Map<Long, Rider> map = new HashMap<>();
        for (Rider rider : riderRepository.findAllById(riderIds)) {
            map.put(rider.getId(), rider);
        }
        return map;
    }

    private Page<Settlement> toPage(List<Settlement> fullList, Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int fromIndex = Math.min(pageNumber * pageSize, fullList.size());
        int toIndex = Math.min(fromIndex + pageSize, fullList.size());
        List<Settlement> content = fullList.subList(fromIndex, toIndex);
        return new PageImpl<>(content, pageable, fullList.size());
    }
}
