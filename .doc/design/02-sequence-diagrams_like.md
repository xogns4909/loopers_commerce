### ❤️ 좋아요 (Like)

```mermaid
sequenceDiagram
actor U as 사용자
participant LC as LikeController
participant LF as LikeFacade
participant LS as LikeService
participant PS as ProductService
participant PR as ProductRepository
participant LR as LikeRepository

U ->> LC: 좋아요 토글 요청 (productId)
LC ->> LF: 좋아요 처리 요청
LF ->> PS: 상품 존재 확인
PS ->> PR: 상품 조회
alt 상품 없음
    PR -->> PS: null
    PS -->> LF: 오류
    LF -->> LC: 404 Not Found
    LC -->> U: 실패 응답
else
    PR -->> PS: 상품 있음
    PS -->> LF: 확인 완료
    LF ->> LS: 좋아요 처리 위임
    LS ->> LR: 좋아요 여부 확인
    alt 이미 좋아요함
        LR -->> LS: 존재
        LS ->> LR: 삭제 요청
        LR -->> LS: 완료
        LS -->> LF: 취소됨
    else
        LR -->> LS: 없음
        LS ->> LR: 저장 요청
        LR -->> LS: 완료
        LS -->> LF: 등록됨
    end
    LF -->> LC: 응답 구성
    LC -->> U: 성공 응답
end
```