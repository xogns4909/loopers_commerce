```mermaid
classDiagram

%% --- Product 도메인 ---
class Product {
+Long id
+String name
+String description
+Money price
+Stock stock
+String brandId
+isAvailable(): boolean
+checkPurchasable(quantity: int): void
}
class Stock {
+int quantity
+isEnough(amount: int): boolean
+decrease(amount: int): void
}
class Brand {
+Long id
+String name
+String logoUrl
}
class Money {
+BigDecimal value
+add(Money): Money
+subtract(Money): Money
+multiply(int): Money
+isPositive(): boolean
+isEnough(Money): boolean
}

Product --> Stock : 소유
Product --> Money : 소유 (price)
Product --> Brand : 참조

%% --- User 도메인 ---
class User {
+Long id
+Email email
+Gender gender
+BirthDay birthDay
+Point point
}
class Email {
+String value
+isValid(): boolean
}
class Gender {
<<enum>>
M
F
}
class BirthDay {
+LocalDate value
+isValid(): boolean
}
class Point {
+Money balance
+charge(amount: Money): void
+deduct(amount: Money): void
+isEnough(amount: Money): boolean
+currentAmount(): Money
}

User --> Email : 소유
User --> Gender : 소유
User --> BirthDay : 소유
User --> Point : 소유
Point --> Money : 소유 (balance)

%% --- 주문 도메인 ---
class Order {
+Long id
+Long userId
+List~OrderItem~ items
+OrderStatus status
+Money originalAmount
+Money discountedAmount
+create(): Order
+complete(): void
+cancel(): void
+calculateOriginalAmount(): Money
+calculateDiscountedAmount(): Money
}
class OrderItem {
+Long productId
+int quantity
+Money subtotal
+List~Discount~ discounts
+calculateSubtotal(): Money
+calculateDiscountedSubtotal(): Money
}
class Discount {
+DiscountType type
+String reason
+BigDecimal rate
+Money fixedAmount
}
class DiscountType {
<<enum>>
RATE
FIXED
COUPON
PROMOTION
}
class OrderStatus {
<<enum>>
CREATED
COMPLETED
CANCELLED
}

Order --> OrderItem : 소유
Order --> OrderStatus : 소유
Order --> User : 참조
OrderItem --> Discount : 소유
OrderItem --> Product : 참조
Discount --> DiscountType : 분류

%% --- 결제 도메인 ---
class Payment {
+Long orderId
+Money amount
+PaymentMethod method
+PaymentStatus status
+String failedReason
+LocalDateTime approvedAt
+markSuccess(): void
+markFailed(reason: String): void
+isSuccess(): boolean
}
class PaymentMethod {
<<enum>>
POINT
CARD
KAKAO_PAY
}
class PaymentStatus {
<<enum>>
PENDING
SUCCESS
FAILED
CANCELLED
}

Payment --> Order : 참조
Payment --> Money : 소유 (amount)
Payment --> PaymentMethod : 소유
Payment --> PaymentStatus : 소유

%% --- 좋아요 도메인 ---
class Like {
+Long userId
+Long productId
+LocalDateTime likedAt
}

Like --> User : 참조
Like --> Product : 참조
