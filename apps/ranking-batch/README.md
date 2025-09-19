# Ranking Batch

Spring Batch 기반 상품 랭킹 시스템

## 기능
- Daily 랭킹 집계 (event_metrics → mv_product_rank_daily)  
- Weekly Top 100 랭킹 생성 (7일치 집계)
- Monthly Top 100 랭킹 생성 (30일치 집계)
- Redis 캐시 발행

## 배치 실행 스케줄
- Daily: 매일 새벽 2시
- Weekly: 매주 월요일 새벽 3시  
- Monthly: 매월 1일 새벽 4시

## 실행 방법
```bash
# 애플리케이션 실행
./gradlew :apps:ranking-batch:bootRun

# 수동 배치 실행 (개발용)
# Daily: java -jar ranking-batch.jar --job.name=dailyRankingJob date=2024-09-18
# Weekly: java -jar ranking-batch.jar --job.name=weeklyRankingJob yearWeek=2024W38
# Monthly: java -jar ranking-batch.jar --job.name=monthlyRankingJob yearMonth=202409
```

## 주요 클래스
- `RankingBatchApplication.java`: 메인 애플리케이션
- `DailyRankingJobConfig.java`: Daily 집계 배치
- `WeeklyRankingJobConfig.java`: Weekly Top 100 배치
- `MonthlyRankingJobConfig.java`: Monthly Top 100 배치
