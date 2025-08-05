package com.loopers.application.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.user.model.UserId;
import com.loopers.interfaces.api.order.OrderDetailResponse;
import com.loopers.interfaces.api.order.OrderSummaryResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order createOrder(UserId userId, List<OrderItemCommand> items) {
        List<OrderItem> orderItems = items.stream()
            .map(item -> new OrderItem(item.productId(), item.quantity(), item.price()))
            .toList();

        Order order = Order.create(userId, orderItems);
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems, savedOrder.getId());

        return savedOrder;
    }

    @Override
    public void completeOrder(Order order) {
        order.complete();
        orderRepository.save(order);
    }

    @Override
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
}

