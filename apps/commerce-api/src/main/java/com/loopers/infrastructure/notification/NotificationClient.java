package com.loopers.infrastructure.notification;

import java.util.Map;

public interface NotificationClient {
    void sendKakao(String userId, String templateId, String locale, Map<String, String> variables);
}
