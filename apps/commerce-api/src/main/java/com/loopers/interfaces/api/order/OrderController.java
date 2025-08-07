package com.loopers.interfaces.api.order;



import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderDetailCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderSearchCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.UserCertifyUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
        @RequestHeader("X-USER-ID") String userId,
        @RequestHeader("X-IDEMPOTENCY-KEY") String idempotencyKey,
        @RequestBody OrderRequest request
    ) {

        UserCertifyUtil.extractUserId(userId);
        OrderCommand command = request.toCommand(userId,idempotencyKey);
        OrderResponse response = orderFacade.order(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getUserOrders(
        @RequestHeader("X-USER-ID") String userId,
        Pageable pageable
    ) {

        UserCertifyUtil.extractUserId(userId);
        OrderSearchCommand command = new OrderSearchCommand(userId, pageable);
        Page<OrderSummaryResponse> response = orderFacade.getUserOrders(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
        @RequestHeader("X-USER-ID") String userId,
        @PathVariable Long orderId
    ) {
        UserCertifyUtil.extractUserId(userId);
        OrderDetailCommand command = new OrderDetailCommand(userId, orderId);
        OrderDetailResponse response = orderFacade.getOrderDetail(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
