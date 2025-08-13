// 인덱스 Before/After 비교 전용 테스트
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// 시나리오별 성능 메트릭
const shallowPagesTime = new Trend('shallow_pages_time');
const mediumPagesTime = new Trend('medium_pages_time'); 
const deepPagesTime = new Trend('deep_pages_time');
const extremePagesTime = new Trend('extreme_pages_time');

const latestSortTime = new Trend('latest_sort_time');
const priceSortTime = new Trend('price_sort_time');
const likesSortTime = new Trend('likes_sort_time');

const brandFilterTime = new Trend('brand_filter_time');
const noBrandFilterTime = new Trend('no_brand_filter_time');

const slowQueriesCounter = new Counter('slow_queries_total');
const verySlowQueriesCounter = new Counter('very_slow_queries_total');

export const options = {
  scenarios: {
    // 인덱스 성능 측정에 최적화된 시나리오
    index_performance: {
      executor: 'constant-vus',
      vus: 1,           // 50명 동시 사용자
      duration: '30s',    // 정확히 3분으로 고정
    }
  },
  thresholds: {
    // 성능 기준점 설정 (인덱스 전후 비교용)
    shallow_pages_time: ['p(95)<2000'],   // 얕은 페이지: 2초 이하
    medium_pages_time: ['p(95)<5000'],    // 중간 페이지: 5초 이하  
    deep_pages_time: ['p(95)<10000'],     // 깊은 페이지: 10초 이하
    extreme_pages_time: ['p(95)<20000'],  // 극한 페이지: 20초 이하
    
    latest_sort_time: ['p(95)<3000'],     // LATEST 정렬
    price_sort_time: ['p(95)<3000'],      // PRICE 정렬
    likes_sort_time: ['p(95)<5000'],      // LIKES 정렬 (복잡)
    
    brand_filter_time: ['p(95)<3000'],    // 브랜드 필터링
    no_brand_filter_time: ['p(95)<2000'], // 필터링 없음
  },
};

const BASE_URL = 'http://host.docker.internal:8080';

export default function() {
  // 페이지 깊이별 테스트 (인덱스 효과가 극명하게 나타나는 부분)
  const pageScenarios = [
    { name: 'shallow', pages: [0, 1, 2], metric: shallowPagesTime },
    { name: 'medium', pages: [10, 20, 50], metric: mediumPagesTime },
    { name: 'deep', pages: [100, 200, 500], metric: deepPagesTime },
    { name: 'extreme', pages: [1000, 2000, 5000], metric: extremePagesTime }
  ];
  
  const pageScenario = pageScenarios[Math.floor(Math.random() * pageScenarios.length)];
  const page = pageScenario.pages[Math.floor(Math.random() * pageScenario.pages.length)];
  
  // 정렬 방식별 테스트
  const sortOptions = [
    { sort: 'LATEST', metric: latestSortTime },
    { sort: 'PRICE_DESC', metric: priceSortTime },
    { sort: 'LIKES_DESC', metric: likesSortTime }
  ];
  
  const sortOption = sortOptions[Math.floor(Math.random() * sortOptions.length)];
  
  // 브랜드 필터링 유무 테스트 (50% 확률)
  const useBrandFilter = Math.random() < 0.5;
  const brandId = useBrandFilter ? Math.floor(Math.random() * 100) + 1 : null;
  
  // URL 구성
  let url = `${BASE_URL}/api/v1/products?page=${page}&size=20&sortBy=${sortOption.sort}`;
  if (brandId) {
    url += `&brandId=${brandId}`;
  }
  
  const startTime = Date.now();
  const response = http.get(url, { timeout: '30s' });
  const duration = response.timings.duration;
  
  // 메트릭 기록
  pageScenario.metric.add(duration);
  sortOption.metric.add(duration);
  
  if (brandId) {
    brandFilterTime.add(duration);
  } else {
    noBrandFilterTime.add(duration);
  }
  
  // 성능 분류
  if (duration > 10000) {
    verySlowQueriesCounter.add(1);
    console.log(`🐌 VERY SLOW: ${pageScenario.name} page ${page}, ${sortOption.sort}, brand=${brandId} - ${duration}ms`);
  } else if (duration > 5000) {
    slowQueriesCounter.add(1);
    console.log(`⚠️ SLOW: ${pageScenario.name} page ${page}, ${sortOption.sort}, brand=${brandId} - ${duration}ms`);
  }
  
  check(response, {
    [`${pageScenario.name}_page_success`]: (r) => r.status === 200,
    [`${sortOption.sort}_sort_success`]: (r) => r.status === 200,
    'response_under_30s': (r) => r.timings.duration < 30000,
  });
  
  sleep(0.1 + Math.random() * 0.2);
}

export function setup() {
  console.log('📊 INDEX COMPARISON TEST');
  console.log('🔍 이 테스트를 인덱스 전후로 실행하여 성능 차이를 측정하세요!');
  console.log('');
  console.log('📋 측정 항목:');
  console.log('  - 페이지 깊이별 성능 (0~5000 페이지)');
  console.log('  - 정렬 방식별 성능 (LATEST, PRICE_DESC, LIKES_DESC)');
  console.log('  - 브랜드 필터링 유무별 성능');
  console.log('');
  console.log('⚡ 예상 인덱스 효과:');
  console.log('  - 얕은 페이지: 50-80% 성능 향상');
  console.log('  - 깊은 페이지: 10-30% 성능 향상');
  console.log('  - 브랜드 필터링: 70-90% 성능 향상');
}

export function teardown(data) {
  console.log('');
  console.log('📈 인덱스 비교 테스트 완료!');
  console.log('📊 결과 분석 포인트:');
  console.log('  1. shallow vs deep page 성능 차이');
  console.log('  2. 정렬 방식별 성능 순위');
  console.log('  3. 브랜드 필터링 효과');
  console.log('  4. p95, p99 응답시간 변화');
  console.log('');
  console.log('💡 다음 단계:');
  console.log('  1. 현재 결과를 기록해두세요');
  console.log('  2. 인덱스를 추가하세요');
  console.log('  3. 동일한 테스트를 재실행하세요');
  console.log('  4. 성능 개선 정도를 비교하세요');
}
