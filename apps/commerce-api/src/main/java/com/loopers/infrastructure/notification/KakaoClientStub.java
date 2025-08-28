package com.loopers.infrastructure.notification;


import com.loopers.infrastructure.notification.NotificationClient;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class KakaoClientStub implements NotificationClient {
    @Override
    public void sendKakao(String             userId, String templateId, String locale, Map<String, String> variables) {

        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(150, 400));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

    }
}
