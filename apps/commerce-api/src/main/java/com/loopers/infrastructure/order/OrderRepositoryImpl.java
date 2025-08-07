package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.model.Order;
import com.loopers.infrastructure.order.entity.OrderEntity;
import com.loopers.infrastructure.order.entity.QOrderEntity;
import com.loopers.infrastructure.order.entity.QOrderItemEntity;
import com.loopers.infrastructure.product.entity.QProductEntity;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderItemDetail;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final JPAQueryFactory queryFactory;

    private final QOrderEntity order = QOrderEntity.orderEntity;
    private final QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(OrderEntity.from(order)).toModel();
    }

    @Override
    public Page<OrderSummaryResponse> findOrderSummariesByUserId(String userId, Pageable pageable) {
        List<OrderSummaryResponse> content = queryFactory
            .select(Projections.constructor(
                OrderSummaryResponse.class,
                order.id,
                order.amount,
                order.status,
                order.createdAt
            ))
            .from(order)
            .where(order.userId.eq(userId))
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(order.count())
            .from(order)
            .where(order.userId.eq(userId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public OrderDetailResponse findOrderDetailByUserIdAndOrderId(String userId, Long orderId) {
        OrderEntity orderEntity = queryFactory
            .selectFrom(order)
            .where(order.userId.eq(userId), order.id.eq(orderId))
            .fetchOne();

        if (orderEntity == null) return null;

        QProductEntity product = QProductEntity.productEntity;

        List<OrderItemDetail> items = queryFactory
            .select(Projections.constructor(
                OrderItemDetail.class,
                orderItem.productId,
                product.name,
                orderItem.quantity,
                orderItem.price
            ))
            .from(orderItem)
            .join(product).on(orderItem.productId.eq(product.id))
            .where(orderItem.orderId.eq(orderId))
            .fetch();

        return new OrderDetailResponse(
            orderEntity.getId(),
            orderEntity.getAmount(),
            orderEntity.getStatus(),
            items
        );
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return jpaOrderRepository.findById(orderId).map(OrderEntity::toModel);
    }
}
