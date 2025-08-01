    package com.loopers.interfaces.api.order;

    import com.loopers.domain.order.model.OrderStatus;
    import java.math.BigDecimal;
    import java.util.List;

    public record OrderDetailResponse(
        Long orderId,
        Long amount,
        OrderStatus status,
        List<OrderItemDetail> items
    ) {}
