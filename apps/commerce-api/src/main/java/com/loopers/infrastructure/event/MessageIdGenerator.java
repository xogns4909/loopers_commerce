package com.loopers.infrastructure.event;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageIdGenerator {

    private static final String PREFIX = "evt";

    public String generate() {
        return PREFIX + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    public String generateWithPrefix(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }
}
