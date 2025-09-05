package com.loopers.application.notification;



import com.loopers.infrastructure.notification.NotificationClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender {
    private final NotificationClient client;

    public NotificationSender(NotificationClient client) {
        this.client = client;
    }

    @Async("notificationsExec")
    @EventListener
    public void onRequested(MessageSendRequested req) {
        if (req.channel() == MessageSendRequested.Channel.KAKAO) {
            client.sendKakao(req.recipientUserId(), req.templateId(), req.locale(), req.variables());
        }
    }
}
