### 💸 결제 (Payment)

```mermaid
sequenceDiagram
actor U as 사용자
participant PC as PaymentController
participant PF as PaymentFacade
participant PS as PaymentService
participant OS as OrderService
participant PT as PointService
participant OR as OrderRepository

U ->> PC: 결제 요청 (orderId)
PC ->> PF: 결제 처리 요청
PF ->> PS: 결제 처리 시작
PS -->> PF: 주문 상태 확인 필요
PF ->> OS: 주문 상태 확인 요청
OS ->> OR: 주문 상태 조회
alt 주문 없음 또는 결제 완료
    OR -->> OS: 오류
    OS -->> PF: 실패
    PF -->> PC: 실패 응답
    PC -->> U: 실패 응답
else
    OR -->> OS: 주문 정보 반환
    OS -->> PF: 주문 유효
    PF ->> PT: 포인트 차감 요청
    alt 포인트 부족
        PT -->> PF: 실패
        PF -->> PC: 402 Payment Required
        PC -->> U: 실패 응답
    else
        PT -->> PF: 포인트 차감 완료
        PF ->> OS: 결제 완료 상태 저장 요청
        OS ->> OR: 결제 완료 저장
        OR -->> OS: 저장 완료
        OS -->> PF: 저장 완료
        PF -->> PC: 결제 완료 응답
        PC -->> U: 결제 성공 응답
    end
end