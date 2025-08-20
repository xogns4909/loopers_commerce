package com.loopers.infrastructure.order.entity;
import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.model.Price;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_items")
public class OrderItemEntity extends BaseEntity {


    private Long orderId;


    private Long productId;

    private Long price;

    private int quantity;

    protected OrderItemEntity() {}

    public static OrderItemEntity from(OrderItem item, Long orderId) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.productId = item.getProductId();
        entity.quantity = item.getQuantity();
        entity.orderId = orderId;
        entity.price = item.getUnitPrice().value().longValue();
        return entity;
    }

    public OrderItem toModel() {
        return new OrderItem(productId, quantity, Price.of(BigDecimal.valueOf(price)));
    }


}
