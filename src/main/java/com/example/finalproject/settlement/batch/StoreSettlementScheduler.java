package com.example.finalproject.settlement.batch;

import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreSettlementScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("storeSettlementGenerateJob")
    private final Job storeSettlementGenerateJob;

    @Qualifier("storeSettlementCompleteJob")
    private final Job storeSettlementCompleteJob;

    /**
     * 정산 원장 생성 스케줄.
     * 기본: 매월 20일 02:00
     */
    @Scheduled(cron = "${settlement.store.generate-cron:0 0 2 20 * *}", zone = "Asia/Seoul")
    public void runGenerateJob() {
        launch(storeSettlementGenerateJob, "generate");
    }

    /**
     * 정산 완료 처리 스케줄.
     * 기본: 매월 20일 02:30
     */
    @Scheduled(cron = "${settlement.store.complete-cron:0 30 2 20 * *}", zone = "Asia/Seoul")
    public void runCompleteJob() {
        launch(storeSettlementCompleteJob, "complete");
    }

    private void launch(Job job, String phase) {
        try {
            // targetYearMonth를 넘겨 수동 실행/재실행 시에도 동일 로직 재사용 가능
            String target = YearMonth.now().minusMonths(1).toString();
            JobParameters params = new JobParametersBuilder()
                    .addString("targetYearMonth", target)
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
            log.info("store settlement {} job launched. target={}", phase, target);
        } catch (Exception e) {
            log.error("store settlement {} job failed", phase, e);
        }
    }
}
