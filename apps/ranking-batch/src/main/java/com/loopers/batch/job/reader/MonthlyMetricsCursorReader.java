package com.loopers.batch.job.reader;

import com.loopers.batch.domain.dto.MonthlyMetricsDto;
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

@Configuration
@Slf4j
public class MonthlyMetricsCursorReader {

    private static final String MONTHLY_AGGREGATION_SQL = """
        SELECT 
            d.product_id,
            SUM(d.view_count) AS view_count,
            SUM(d.like_count) AS like_count,
            SUM(d.order_count) AS order_count
        FROM mv_product_rank_daily d
        WHERE d.stat_date >= ? AND d.stat_date <= ?
        GROUP BY d.product_id
        HAVING (view_count + like_count + order_count) > 0
        ORDER BY d.product_id ASC
        """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<MonthlyMetricsDto> monthlyMetricsReader(
        DataSource dataSource,
        @Value("#{jobParameters['targetDate'] ?: '2025-09-30'}") String targetDate
    ) {
        final String finalTarget = (targetDate == null || targetDate.isBlank()) ? "2025-09-30" : targetDate;

        LocalDate target = LocalDate.parse(finalTarget);
        LocalDate startDate = target.withDayOfMonth(1);
        LocalDate endDate = target.withDayOfMonth(target.lengthOfMonth());

        log.info("월간 메트릭 Reader 초기화 - 대상 날짜: {}, 범위: {} ~ {} ({}일)",
            finalTarget, startDate, endDate, endDate.lengthOfMonth());

        return new JdbcCursorItemReaderBuilder<MonthlyMetricsDto>()
            .name("monthlyMetricsReader")
            .dataSource(dataSource)
            .sql(MONTHLY_AGGREGATION_SQL)
            .preparedStatementSetter(ps -> {
                ps.setDate(1, java.sql.Date.valueOf(startDate));
                ps.setDate(2, java.sql.Date.valueOf(endDate));
            })
            .rowMapper(new MonthlyRowMapper(target, startDate, endDate))
            .build();
    }

    private static class MonthlyRowMapper implements RowMapper<MonthlyMetricsDto> {
        private final LocalDate targetDate;
        private final LocalDate periodStartDate;
        private final LocalDate periodEndDate;

        public MonthlyRowMapper(LocalDate targetDate, LocalDate periodStartDate, LocalDate periodEndDate) {
            this.targetDate = targetDate;
            this.periodStartDate = periodStartDate;
            this.periodEndDate = periodEndDate;
        }

        @Override
        public MonthlyMetricsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MonthlyMetricsDto(
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
