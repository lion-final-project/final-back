package com.example.finalproject.settlement.store.batch;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
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
public class StoreSettlementBatchLauncher {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @Qualifier("storeSettlementGenerateJob")
    private final Job storeSettlementGenerateJob;

    @Qualifier("storeSettlementCompleteJob")
    private final Job storeSettlementCompleteJob;

    public int runMonthlyPipeline(YearMonth targetMonth) {
        runGenerateJob(targetMonth);
        return runCompleteJob(targetMonth);
    }

    public void runGenerateJob(YearMonth targetMonth) {
        launch(storeSettlementGenerateJob, targetMonth);
    }

    public int runCompleteJob(YearMonth targetMonth) {
        JobExecution execution = launch(storeSettlementCompleteJob, targetMonth);
        return execution.getExecutionContext().containsKey("completedCount")
                ? execution.getExecutionContext().getInt("completedCount")
                : 0;
    }

    private JobExecution launch(Job job, YearMonth targetMonth) {
        try {
            JobParameters params = buildParameters(targetMonth);
            return jobLauncher.run(job, params);
        } catch (JobExecutionAlreadyRunningException
                 | JobRestartException
                 | JobInstanceAlreadyCompleteException exception) {
            log.warn("store settlement batch skipped. job={}, targetMonth={}, reason={}",
                    job.getName(), targetMonth, exception.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "동일한 정산 배치가 이미 실행 중이거나 완료되었습니다.");
        } catch (Exception exception) {
            log.error("store settlement batch failed. job={}, targetMonth={}", job.getName(), targetMonth, exception);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "정산 배치 실행에 실패했습니다.");
        }
    }

    private JobParameters buildParameters(YearMonth targetMonth) {
        return new JobParametersBuilder(jobExplorer)
                .addString("targetYearMonth", targetMonth.toString())
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();
    }
}
