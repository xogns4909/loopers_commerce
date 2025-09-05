package com.loopers.application.notification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.loopers.application.notification.MessageSendRequested.Channel;
import com.loopers.infrastructure.notification.NotificationClient;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationSenderUnitTest {

    @Test
    void onRequested_calls_Client() {
        NotificationClient client = mock(NotificationClient.class);
        NotificationSender sender = new NotificationSender(client);

        MessageSendRequested req = new MessageSendRequested(
            Channel.KAKAO,
            "ORDER_CREATED_ko",
            "kth4909",
            "ko-KR",
            Map.of("orderId", "100")
        );

        sender.onRequested(req);

        verify(client, times(1))
            .sendKakao(eq("kth4909"), eq("ORDER_CREATED_ko"), eq("ko-KR"), argThat(m -> "100".equals(m.get("orderId"))));
    }
}
