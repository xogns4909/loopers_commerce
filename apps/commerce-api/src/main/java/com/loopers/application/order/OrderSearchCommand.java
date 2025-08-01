package com.loopers.application.order;


import org.springframework.data.domain.Pageable;

public record OrderSearchCommand(
    String userId,
    Pageable pageable
) {}
