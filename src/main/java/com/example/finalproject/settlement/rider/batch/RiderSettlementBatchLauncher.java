package com.example.finalproject.settlement.rider.batch;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiderSettlementBatchLauncher {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @Qualifier("riderWeeklySettlementJob")
    private final Job riderWeeklySettlementJob;

    public int runMonthlyPipeline(YearMonth targetMonth) {
        int launched = 0;
        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        LocalDate weekStart = monthStart.with(DayOfWeek.MONDAY);
        if (weekStart.isAfter(monthStart)) {
            weekStart = weekStart.minusWeeks(1);
        }

        while (!weekStart.isAfter(monthEnd)) {
            if (weekStart.getMonth() == targetMonth.getMonth() && weekStart.getYear() == targetMonth.getYear()) {
                launch(weekStart);
                launched++;
            }
            weekStart = weekStart.plusWeeks(1);
        }

        return launched;
    }

    private void launch(LocalDate targetWeekStart) {
        try {
            JobParameters params = new JobParametersBuilder(jobExplorer)
                    .addString("targetWeekStart", targetWeekStart.toString())
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(riderWeeklySettlementJob, params);
        } catch (JobExecutionAlreadyRunningException
                 | JobRestartException
                 | JobInstanceAlreadyCompleteException exception) {
            log.warn("rider settlement batch skipped. targetWeekStart={}, reason={}",
                    targetWeekStart, exception.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "동일한 라이더 정산 배치가 이미 실행 중이거나 완료되었습니다.");
        } catch (Exception exception) {
            log.error("rider settlement batch failed. targetWeekStart={}", targetWeekStart, exception);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "라이더 정산 배치 실행에 실패했습니다.");
        }
    }
}
