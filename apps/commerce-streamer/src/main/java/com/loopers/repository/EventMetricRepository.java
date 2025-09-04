package com.loopers.repository;

import com.loopers.entity.EventMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMetricRepository extends JpaRepository<EventMetric, Long> {
    
    Optional<EventMetric> findByEventTypeAndMetricNameAndMetricDateAndMetricHour(
        String eventType, String metricName, String metricDate, Integer metricHour
    );
    
    List<EventMetric> findByEventTypeAndMetricDate(String eventType, String metricDate);
    
    List<EventMetric> findByMetricDate(String metricDate);
}
