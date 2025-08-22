package com.loopers.application.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderAmount;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.order.model.OrderStatus;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order createOrder(UserId userId, List<OrderItem> orderItems, OrderAmount orderAmount,Long couponId) {
        Order order = Order.create(userId, orderItems, orderAmount,couponId);
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems, savedOrder.getId());

        return savedOrder;
    }

    @Override
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    @Transactional
    public void markPaid(Order order, Long paymentId) {
        log.info("주문 결제 완료 처리 - orderId: {}, paymentId: {}", order.getId(), paymentId);
        order.complete(); // 기존 complete 메서드 활용 (COMPLETED로 변경)
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void markPaymentFailed(Order order, String reason) {
        log.info("주문 결제 실패 처리 - orderId: {}, reason: {}", order.getId(), reason);
        Order failedOrder = order.fail(); // 기존 fail 메서드 활용 (FAILED로 변경)
        orderRepository.save(failedOrder);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Page<OrderSummaryResponse> getUserOrders(OrderSearchCommand command) {
        return orderRepository.findOrderSummariesByUserId(command.userId(), command.pageable());
    }

    @Override
    public OrderDetailResponse getOrderDetail(OrderDetailCommand command) {
        return orderRepository.findOrderDetailByUserIdAndOrderId(command.userId(), command.orderId());
    }

    @Override
    @Transactional
    public void completeOrder(Order order) {
        order.complete();
        orderRepository.save(order);
    }
}
