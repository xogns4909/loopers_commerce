package com.loopers.application.order;


import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import java.util.List;


public record OrderCommand(
    UserId userId,
    List<OrderItemCommand> items,
    PaymentMethod paymentMethod,
    String CardType,
    String CardNo,
    Price price,
    String idempotencyKey,
    Long couponId
) {
    public record OrderItemCommand(Long productId, int quantity, Price price,String idempotencyKey,Long couponId) {

        public OrderItem toModel() {
            return new OrderItem(productId, quantity, price);
        }
    }
    public List<OrderItem> toItems() {
        return items.stream().map(OrderItemCommand::toModel).toList();
    }
}
