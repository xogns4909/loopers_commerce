package com.loopers.repository;

import com.loopers.entity.EventMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMetricRepository extends JpaRepository<EventMetric, Long> {
    
    @Query("SELECT e FROM EventMetric e WHERE e.eventType = :eventType AND e.metricName = :metricName " +
           "AND e.metricDate = :metricDate AND e.metricHour = :metricHour AND e.productId = :productId")
    Optional<EventMetric> findMetricByKey(@Param("eventType") String eventType, 
                                         @Param("metricName") String metricName,
                                         @Param("metricDate") String metricDate, 
                                         @Param("metricHour") Integer metricHour,
                                         @Param("productId") Long productId);
    
    List<EventMetric> findByEventTypeAndMetricDate(String eventType, String metricDate);
    
    List<EventMetric> findByMetricDate(String metricDate);
    
    List<EventMetric> findByProductIdAndMetricDate(Long productId, String metricDate);
}
