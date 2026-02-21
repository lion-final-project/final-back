package com.example.finalproject.settlement.rider.scheduler;

import com.example.finalproject.settlement.rider.util.SettlementWeekUtil;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 라이더 주간 정산 스케줄러.
 *
 * <p>매주 목요일 02:00 (Asia/Seoul) 에 직전 주(月~日) 정산 Job을 실행한다.</p>
 *
 * <p>JobParameter:
 * <ul>
 *   <li>{@code targetWeekStart} — 직전 주 월요일(yyyy-MM-dd). Config의 Reader가 기간 계산에 사용.</li>
 *   <li>{@code requestedAt} — 실행 시각 밀리초. 동일 주차 재실행 시 새 JobInstance 생성을 위한 구분자.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiderSettlementScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("riderWeeklySettlementJob")
    private final Job riderWeeklySettlementJob;

    /**
     * 매주 목요일 02:00 실행 (Asia/Seoul).
     * cron = "${settlement.rider.cron:0 0 2 * * THU}" — yml로 재정의 가능.
     */
    @Scheduled(cron = "${settlement.rider.cron:0 0 2 * * THU}", zone = "Asia/Seoul")
    public void runRiderWeeklySettlement() {
        String targetWeekStart = SettlementWeekUtil.prevWeekStartDate(LocalDate.now()).toString();
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("targetWeekStart", targetWeekStart)
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(riderWeeklySettlementJob, params);
            log.info("[RiderSettlement] weekly job launched. targetWeekStart={}", targetWeekStart);
        } catch (Exception e) {
            log.error("[RiderSettlement] weekly job failed. targetWeekStart={}", targetWeekStart, e);
        }
    }
}