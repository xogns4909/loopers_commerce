package com.loopers.batch.job.reader;

import com.loopers.batch.domain.dto.DailyMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;


@Configuration
@Slf4j
public class EventMetricsCursorReader {
    
    private static final String DAILY_AGGREGATION_SQL = """
        SELECT 
            em.product_id,
            em.metric_date,
            SUM(CASE WHEN em.event_type = 'ProductLiked' THEN em.metric_value ELSE 0 END) as like_count,
            SUM(CASE WHEN em.event_type = 'OrderCreated' THEN em.metric_value ELSE 0 END) as order_count,
            SUM(CASE WHEN em.event_type = 'ProductViewed' THEN em.metric_value ELSE 0 END) as view_count
        FROM event_metrics em
        WHERE em.metric_date = ?
          AND em.product_id IS NOT NULL
        GROUP BY em.product_id, em.metric_date
        HAVING (like_count + order_count + view_count) > 0
        ORDER BY em.product_id ASC
        """;
    
    @Bean
    @StepScope
    public JdbcCursorItemReader<DailyMetricsDto> dailyMetricsReader(
        DataSource dataSource,
        @Value("#{jobParameters['targetDate'] ?: '2025-09-11'}") String targetDate
    ) {
        log.info("일별 메트릭 Reader 초기화 - 대상 날짜: {}", targetDate);
        
        // null 체크 및 기본값 설정
        final String finalTargetDate;
        if (targetDate == null || targetDate.trim().isEmpty()) {
            finalTargetDate = "2025-09-11"; // 테스트 데이터가 있는 날짜
            log.warn("targetDate가 null이므로 기본값 사용: {}", finalTargetDate);
        } else {
            finalTargetDate = targetDate;
        }
        
        LocalDate statDate = LocalDate.parse(finalTargetDate);
        
        return new JdbcCursorItemReaderBuilder<DailyMetricsDto>()
            .name("dailyMetricsReader")
            .dataSource(dataSource)
            .sql(DAILY_AGGREGATION_SQL)
            .preparedStatementSetter(ps -> ps.setString(1, finalTargetDate))
            .rowMapper(new DailyMetricsRowMapper(statDate))
            .build();
    }
    
    /**
     * ResultSet을 DailyMetricsDto로 변환하는 RowMapper
     */
    private static class DailyMetricsRowMapper implements RowMapper<DailyMetricsDto> {
        
        private final LocalDate statDate;
        
        public DailyMetricsRowMapper(LocalDate statDate) {
            this.statDate = statDate;
        }
        
        @Override
        public DailyMetricsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DailyMetricsDto(
                rs.getLong("product_id"),
                statDate,
                rs.getLong("like_count"),
                rs.getLong("order_count"),
                rs.getLong("view_count")
            );
        }
    }
}
