package com.loopers.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "event_metrics", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_type", "metric_name", "metric_date", "metric_hour"})
    },
    indexes = {
        @Index(name = "idx_event_type_date", columnList = "event_type, metric_date"),
        @Index(name = "idx_metric_name_date", columnList = "metric_name, metric_date")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;
    
    @Column(name = "metric_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal metricValue;
    
    @Column(name = "metric_date", nullable = false, length = 10)
    private String metricDate;
    
    @Column(name = "metric_hour", nullable = false)
    private Integer metricHour;
    
    @Column(name = "tags", length = 1000)
    private String tags;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Builder
    public EventMetric(String eventType, String metricName, BigDecimal metricValue, 
                      String metricDate, Integer metricHour, String tags) {
        this.eventType = eventType;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.metricDate = metricDate;
        this.metricHour = metricHour;
        this.tags = tags;
    }
    
    public void incrementValue(BigDecimal amount) {
        this.metricValue = this.metricValue.add(amount);
    }
}
