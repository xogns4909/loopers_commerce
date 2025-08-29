package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.product.model.Price;
import com.loopers.domain.user.model.UserId;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

public class OrderRequest {

    @Getter
    private List<OrderItemRequest> items;
    @Getter
    private String paymentMethod;
    private Price price;

    private String cardType;

    private String cardNo;

    private Long couponId;

    public OrderRequest() {
    }

    public OrderRequest(List<OrderItemRequest> items, String paymentMethod) {
        this.items = items;
        this.paymentMethod = paymentMethod;
    }

    public OrderCommand toCommand(String userId, String idempotencyKey) {
        List<OrderCommand.OrderItemCommand> itemCommands = items.stream()
            .map(i -> new OrderCommand.OrderItemCommand(
                i.getProductId(),
                i.getQuantity(),
                Price.of(BigDecimal.valueOf(i.getPrice())),
                idempotencyKey,
                couponId
            ))
            .toList();

        BigDecimal total = itemCommands.stream()
            .map(i -> i.price().value().multiply(BigDecimal.valueOf(i.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderCommand(
            UserId.of(userId),
            itemCommands,
            PaymentMethod.valueOf(paymentMethod),
            cardType,
            cardNo,
            Price.of(total),
            idempotencyKey,
            couponId
        );
    }
}
