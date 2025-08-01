package com.loopers.application.order;

import com.loopers.application.order.OrderCommand.OrderItemCommand;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.user.model.UserId;
import com.loopers.infrastructure.order.entity.OrderItemEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;


    @Transactional
    public Order createOrder(UserId userId, List<OrderItemCommand> items) {
        List<OrderItem> orderItems = items.stream()
            .map(item -> new OrderItem(item.productId(), item.quantity(), item.price()))
            .toList();

        Order order = Order.create(userId, orderItems);
        Order savedOrder = orderRepository.save(order);
        // ID만 넘겨서 저장
        orderItemRepository.saveAll(orderItems, savedOrder.getId());

        return savedOrder;
    }


    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }
}
