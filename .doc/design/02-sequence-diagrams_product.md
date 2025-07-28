### 📦 상품 (Product)



```mermaid
sequenceDiagram
    actor U as 사용자
    participant PC as ProductController
    participant PF as ProductFacade
    participant PS as ProductService
    participant PR as ProductRepository
    participant DP as DiscountPolicyService

    U ->> PC: 상품 목록 조회 요청 (sort, brandId, page, size)
    PC ->> PF: 상품 목록 조회 요청
    PF ->> PS: 정렬 및 페이지 유효성 검증
    alt 오류
        PS -->> PF: 400 Bad Request
        PF -->> PC: 실패 응답
        PC -->> U: 실패 응답
    else 정상
        PS ->> PR: 상품 목록 DB 조회
        PR -->> PS: 상품 목록 반환
        PS ->> DP: 각 상품에 대한 할인 가격 계산 (옵션)
        DP -->> PS: 할인 가격 및 정책 정보 반환
        PS -->> PF: 응답 DTO 구성 (할인 가격 포함)
        PF -->> PC: 응답 반환
        PC -->> U: 상품 목록 응답
    end

    U ->> PC: 상품 상세 조회 요청 (productId)
    PC ->> PF: 상품 상세 조회 요청
    PF ->> PS: 상품 상태 및 존재 확인
    alt 존재하지 않음 또는 판매중 아님
        PS -->> PF: 404 Not Found
        PF -->> PC: 실패 응답
        PC -->> U: 실패 응답
    else
        PS ->> PR: 상품 상세 조회
        PR -->> PS: 상세 정보
        PS ->> DP: 해당 상품에 적용 가능한 할인 조회
        DP -->> PS: 할인 정보 반환
        PS -->> PF: 응답 DTO 구성 (할인 포함)
        PF -->> PC: 상세 응답
        PC -->> U: 상품 상세 응답
    end
```