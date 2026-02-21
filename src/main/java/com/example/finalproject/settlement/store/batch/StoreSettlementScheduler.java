package com.example.finalproject.settlement.store.batch;

import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreSettlementScheduler {

    private final StoreSettlementBatchLauncher batchLauncher;

    @Value("${settlement.store.scheduler-enabled:true}")
    private boolean schedulerEnabled;

    /**
     * 정산 생성 스케줄.
     * 기본값: 매월 20일 02:00
     */
    @Scheduled(cron = "${settlement.store.generate-cron:0 0 2 20 * *}", zone = "Asia/Seoul")
    public void runGenerateJob() {
        if (!schedulerEnabled) {
            return;
        }

        YearMonth target = YearMonth.now().minusMonths(1);
        batchLauncher.runGenerateJob(target);
        log.info("store settlement generate job launched. target={}", target);
    }

    /**
     * 정산 완료 처리 스케줄.
     * 기본값: 매월 20일 02:30
     */
    @Scheduled(cron = "${settlement.store.complete-cron:0 30 2 20 * *}", zone = "Asia/Seoul")
    public void runCompleteJob() {
        if (!schedulerEnabled) {
            return;
        }

        YearMonth target = YearMonth.now().minusMonths(1);
        int completedCount = batchLauncher.runCompleteJob(target);
        log.info("store settlement complete job launched. target={}, completedCount={}", target, completedCount);
    }
}
