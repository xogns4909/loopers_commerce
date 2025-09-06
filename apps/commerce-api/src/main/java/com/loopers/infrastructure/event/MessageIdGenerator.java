package com.loopers.infrastructure.event;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;


@Component
public class MessageIdGenerator {

    private static final String PREFIX = "evt";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        return PREFIX + "_" + generateUUIDv7().toString().replace("-", "");
    }

    public String generateWithPrefix(String prefix) {
        return prefix + "_" + generateUUIDv7().toString().replace("-", "");
    }


    private UUID generateUUIDv7() {
        long timestamp = Instant.now().toEpochMilli();
        
        // 상위 32비트: 타임스탬프의 상위 32비트
        long mostSigBits = (timestamp << 16) | (RANDOM.nextInt(0xFFFF));
        
        // 하위 64비트: 버전(7) + 랜덤값
        long leastSigBits = (7L << 60) | (2L << 62) | (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);
        
        return new UUID(mostSigBits, leastSigBits);
    }


    public long extractTimestamp(String messageId) {
        if (!messageId.startsWith(PREFIX + "_")) {
            throw new IllegalArgumentException("Invalid message ID format");
        }
        
        String uuidPart = messageId.substring(PREFIX.length() + 1);
        String formatted = uuidPart.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
        UUID uuid = UUID.fromString(formatted);
        
        return uuid.getMostSignificantBits() >>> 16;
    }
}
