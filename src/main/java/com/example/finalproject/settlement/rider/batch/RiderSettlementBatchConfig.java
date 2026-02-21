package com.example.finalproject.settlement.rider.batch;

import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.settlement.rider.dto.RiderSettlementDto;
import com.example.finalproject.settlement.rider.repository.RiderSettlementDetailRepository;
import com.example.finalproject.settlement.rider.util.SettlementWeekUtil;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 라이더 주간 정산 Spring Batch 5.x Job 구성.
 *
 * <p>처리 기간은 Scheduler가 전달하는 JobParameter {@code targetWeekStart}(yyyy-MM-dd)에서 파싱한다.
 * {@code @StepScope}를 사용하므로 날짜 계산이 Step 실행 시점에 이루어져 재실행 안전성이 보장된다.
 * 파라미터가 없으면 실행 시점 기준 직전 주로 fallback한다.</p>
 *
 * <p>chunk size = {@code settlement.rider.chunk-size} (기본값 100) : 라이더 N명 단위로 조회 → 처리 → 저장.</p>
 */
@Configuration
@RequiredArgsConstructor
public class RiderSettlementBatchConfig {

    @Value("${settlement.rider.chunk-size:100}")
    private int chunkSize;

    private final JPAQueryFactory queryFactory;
    private final SettlementRepository settlementRepository;
    private final RiderSettlementDetailRepository riderSettlementDetailRepository;
    private final DeliveryRepository deliveryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    public Job riderWeeklySettlementJob(
            JobRepository jobRepository,
            Step riderWeeklySettlementStep
    ) {
        return new JobBuilder("riderWeeklySettlementJob", jobRepository)
                .start(riderWeeklySettlementStep)
                .build();
    }

    @Bean
    public Step riderWeeklySettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            RiderSettlementItemReader riderSettlementItemReader
    ) {
        return new StepBuilder("riderWeeklySettlementStep", jobRepository)
                .<RiderSettlementDto, RiderSettlementDto>chunk(chunkSize, transactionManager)
                .reader(riderSettlementItemReader)
                .processor(riderSettlementItemProcessor())
                .writer(riderSettlementItemWriter())
                .build();
    }

    /**
     * Reader 빈 — {@code @StepScope} 로 Step 실행 시점에 생성됨.
     *
     * <p>JobParameter {@code targetWeekStart}(직전 주 월요일, yyyy-MM-dd)를 파싱하여 기간을 결정한다.
     * 파라미터가 없으면 실행 시점 기준 직전 주 월요일로 fallback.</p>
     */
    @Bean
    @StepScope
    public RiderSettlementItemReader riderSettlementItemReader(
            @Value("#{jobParameters['targetWeekStart']}") String targetWeekStart
    ) {
        LocalDate periodStart = (targetWeekStart != null && !targetWeekStart.isBlank())
                ? LocalDate.parse(targetWeekStart)
                : SettlementWeekUtil.prevWeekStartDate(LocalDate.now());

        LocalDate periodEnd = periodStart.plusDays(6);          // 월요일 + 6일 = 일요일
        LocalDateTime weekStart = periodStart.atStartOfDay();
        LocalDateTime weekEnd = periodEnd.atTime(LocalTime.MAX);

        return new RiderSettlementItemReader(
                queryFactory, chunkSize, weekStart, weekEnd, periodStart, periodEnd);
    }

    @Bean
    public RiderSettlementItemProcessor riderSettlementItemProcessor() {
        return new RiderSettlementItemProcessor(settlementRepository);
    }

    @Bean
    public RiderSettlementItemWriter riderSettlementItemWriter() {
        return new RiderSettlementItemWriter(
                settlementRepository,
                riderSettlementDetailRepository,
                deliveryRepository,
                jdbcTemplate);
    }
}
