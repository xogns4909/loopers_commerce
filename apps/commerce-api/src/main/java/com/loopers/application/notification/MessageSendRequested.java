package com.loopers.application.notification;

import java.util.Map;

public record MessageSendRequested(
    Channel channel,
    String templateId,
    String recipientUserId,
    String locale,
    Map<String, String> variables
) {
    public enum Channel { KAKAO }
}
