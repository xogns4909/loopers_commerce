
```mermaid
erDiagram

%% --- 유저 ---
USER {
bigint id PK
varchar email
varchar gender
date birth_day
}

%% --- 포인트 ---
POINT {
bigint id PK
bigint user_id FK
decimal balance
}

USER ||--|| POINT : has_one

%% --- 브랜드 ---
BRAND {
bigint id PK
varchar name
varchar logo_url
}

%% --- 상품 ---
PRODUCT {
bigint id PK
varchar name
varchar description
decimal price
int stock_quantity
bigint brand_id FK
}

PRODUCT ||--|| BRAND : belongs_to

%% --- 좋아요 ---
LIKE {
bigint user_id FK
bigint product_id FK
datetime liked_at
}

LIKE ||--|| USER : references
LIKE ||--|| PRODUCT : references

%% --- 주문 ---
ORDER {
bigint id PK
bigint user_id FK
varchar status
decimal original_amount
decimal discounted_amount
}

ORDER ||--|| USER : belongs_to

ORDER_ITEM {
bigint id PK
bigint order_id FK
bigint product_id FK
int quantity
decimal unit_price
decimal subtotal
}

ORDER ||--o{ ORDER_ITEM : contains
ORDER_ITEM ||--|| PRODUCT : references

%% --- 할인 정책 ---
DISCOUNT_POLICY {
bigint id PK
varchar name
varchar type
decimal rate
decimal fixed_amount
decimal min_purchase_amount
datetime start_at
datetime end_at
boolean is_active
}

DISCOUNT_POLICY_PRODUCT {
bigint discount_policy_id FK
bigint product_id FK
}

DISCOUNT_POLICY ||--o{ DISCOUNT_POLICY_PRODUCT : targets
PRODUCT ||--o{ DISCOUNT_POLICY_PRODUCT : eligible_for

ORDER_ITEM_DISCOUNT {
bigint id PK
bigint order_item_id FK
bigint discount_policy_id FK
decimal discount_amount
varchar reason
}

ORDER_ITEM ||--o{ ORDER_ITEM_DISCOUNT : has
DISCOUNT_POLICY ||--o{ ORDER_ITEM_DISCOUNT : applied

%% --- 결제 ---
PAYMENT {
bigint id PK
bigint order_id FK
decimal amount
varchar method
varchar status
varchar failed_reason
datetime approved_at
}

ORDER ||--o{ PAYMENT : has_many
