package com.loopers.batch.job.reader;

import com.loopers.batch.domain.dto.WeeklyMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;


@Configuration
@Slf4j
public class WeeklyMetricsCursorReader {
    
    private static final String WEEKLY_AGGREGATION_SQL = """
        SELECT 
            d.product_id,
            SUM(d.view_count) as view_count,
            SUM(d.like_count) as like_count,
            SUM(d.order_count) as order_count
        FROM mv_product_rank_daily d
        WHERE d.stat_date >= ? AND d.stat_date <= ?
        GROUP BY d.product_id
        HAVING (view_count + like_count + order_count) > 0
        ORDER BY d.product_id ASC
        """;
    
    @Bean
    @StepScope
    public JdbcCursorItemReader<WeeklyMetricsDto> weeklyMetricsReader(
        DataSource dataSource,
        @Value("#{jobParameters['targetDate'] ?: '2025-09-18'}") String targetDate
    ) {
        log.info("주간 메트릭 Reader 초기화 - 대상 날짜: {}", targetDate);
        
        // null 체크 및 기본값 설정
        final String finalTargetDate;
        if (targetDate == null || targetDate.trim().isEmpty()) {
            finalTargetDate = "2025-09-18"; // 기본값
            log.warn("targetDate가 null이므로 기본값 사용: {}", finalTargetDate);
        } else {
            finalTargetDate = targetDate;
        }
        
        LocalDate endDate = LocalDate.parse(finalTargetDate);  
        LocalDate startDate = endDate.minusDays(6); // 7일간 (오늘 포함)
        
        log.info("주간 슬라이딩 윈도우: {} ~ {} ({}일간)", startDate, endDate, 7);
        
        return new JdbcCursorItemReaderBuilder<WeeklyMetricsDto>()
            .name("weeklyMetricsReader")
            .dataSource(dataSource)
            .sql(WEEKLY_AGGREGATION_SQL)
            .preparedStatementSetter(ps -> {
                ps.setDate(1, java.sql.Date.valueOf(startDate));
                ps.setDate(2, java.sql.Date.valueOf(endDate));
            })
            .rowMapper(new WeeklyMetricsRowMapper(endDate, startDate, endDate))
            .build();
    }
    

    private static class WeeklyMetricsRowMapper implements RowMapper<WeeklyMetricsDto> {
        
        private final LocalDate targetDate;
        private final LocalDate periodStartDate;
        private final LocalDate periodEndDate;
        
        public WeeklyMetricsRowMapper(LocalDate targetDate, LocalDate periodStartDate, LocalDate periodEndDate) {
            this.targetDate = targetDate;
            this.periodStartDate = periodStartDate;
            this.periodEndDate = periodEndDate;
        }
        
        @Override
        public WeeklyMetricsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new WeeklyMetricsDto(
                rs.getLong("product_id"),
                targetDate,
                periodStartDate,
                periodEndDate,
                rs.getLong("view_count"),
                rs.getLong("like_count"),
                rs.getLong("order_count")
            );
        }
    }
}
