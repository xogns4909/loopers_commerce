package com.loopers.interfaces.api.order;



import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderFacade;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.UserCertifyUtil;
import lombok.RequiredArgsConstructor;
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
        @RequestBody OrderRequest request
    ) {

        UserCertifyUtil.extractUserId(userId);
        OrderCommand command = request.toCommand(userId);
        OrderResponse response = orderFacade.order(command);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
