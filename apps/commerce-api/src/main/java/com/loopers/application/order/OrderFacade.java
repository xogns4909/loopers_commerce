package com.loopers.application.order;


import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.order.OrderResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ProductService productService;

    @Transactional
    public OrderResponse order(OrderCommand command) {

        productService.checkAndDeduct(command.items());

        Order order = orderService.createOrder(command.userId(), command.items());


        paymentService.pay(new PaymentCommand(command.userId(), order.getId(), order.getAmount(), command.paymentMethod()));

        order.complete();
        orderService.save(order);

        return new OrderResponse(order.getId(), order.getAmount().value(),order.getStatus());
    }
}

