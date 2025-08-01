package com.loopers.application.order;
import java.awt.print.Pageable;

public record OrderDetailCommand(
    String userId,
    Long orderId
) {}
