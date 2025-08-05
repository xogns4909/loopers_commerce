package com.loopers.application.order;


import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import java.util.List;


public record OrderCommand(
    UserId userId,
    List<OrderItemCommand> items,
    PaymentMethod paymentMethod,
    Price price,
    String idempotencyKey
) {
    public record OrderItemCommand(Long productId, int quantity, Price price,String idempotencyKey) {}
}
