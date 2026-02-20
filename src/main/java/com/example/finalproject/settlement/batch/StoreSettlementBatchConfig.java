package com.example.finalproject.settlement.batch;

import com.example.finalproject.settlement.service.StoreSettlementService;
import java.time.YearMonth;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StoreSettlementBatchConfig {

    /**
     * 월 정산 원장 생성 Job.
     * - 마트별 정산 마스터/상세를 생성한다.
     */
    @Bean
    public Job storeSettlementGenerateJob(
            JobRepository jobRepository,
            Step storeSettlementGenerateStep
    ) {
        return new JobBuilder("storeSettlementGenerateJob", jobRepository)
                .start(storeSettlementGenerateStep)
                .build();
    }

    @Bean
    public Step storeSettlementGenerateStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            StoreSettlementService storeSettlementService
    ) {
        return new StepBuilder("storeSettlementGenerateStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // targetYearMonth(yyyy-MM) 파라미터가 없으면 전월 기준으로 생성
                    YearMonth target = resolveTargetMonth(chunkContext);
                    storeSettlementService.generateMonthlySettlements(target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    /**
     * 생성된 정산을 완료 처리하는 Job.
     * - 현재는 실제 이체 연동 대신 상태 완료 처리만 수행한다.
     */
    @Bean
    public Job storeSettlementCompleteJob(
            JobRepository jobRepository,
            Step storeSettlementCompleteStep
    ) {
        return new JobBuilder("storeSettlementCompleteJob", jobRepository)
                .start(storeSettlementCompleteStep)
                .build();
    }

    @Bean
    public Step storeSettlementCompleteStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            StoreSettlementService storeSettlementService
    ) {
        return new StepBuilder("storeSettlementCompleteStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // targetYearMonth(yyyy-MM) 파라미터가 없으면 전월 기준으로 완료 처리
                    YearMonth target = resolveTargetMonth(chunkContext);
                    storeSettlementService.completePendingSettlements(target);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private YearMonth resolveTargetMonth(org.springframework.batch.core.scope.context.ChunkContext chunkContext) {
        String yearMonth = (String) chunkContext.getStepContext()
                .getJobParameters()
                .get("targetYearMonth");
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now().minusMonths(1);
        }
        return YearMonth.parse(yearMonth);
    }
}
