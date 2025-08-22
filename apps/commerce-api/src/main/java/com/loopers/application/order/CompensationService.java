package com.loopers.application.order;

import com.loopers.domain.discount.CouponService;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.model.Order;
import com.loopers.domain.order.model.OrderItem;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensationService {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reverseFor(Order order) {
        restoreStock(order.getId());
        if(order.getUsedCouponId()!=null) {
            releaseCoupons(order.getUserId(), order.getUsedCouponId());
        }
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> orderItems = orderService.getOrder(orderId).getItems();
        for (OrderItem item : orderItems) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
    }


    private void releaseCoupons(UserId userId,Long  getUsedCouponId) {
        couponService.releaseSpecificCoupon(getUsedCouponId,userId.value());
    }
}
