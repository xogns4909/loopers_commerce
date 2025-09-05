package com.loopers.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class EnvelopeFactory {

    private final MessageIdGenerator messageIdGenerator;

    @Value("${spring.application.name:commerce-api}")
    private String applicationName;

    public <T> Envelope<T> create(EventType eventType, T payload) {
        return create(eventType.getValue(), payload);
    }

    public <T> Envelope<T> create(String eventType, T payload) {
        String messageId = messageIdGenerator.generate();
        String correlationId = extractCorrelationId();

        return Envelope.create(messageId, eventType, payload, applicationName, correlationId);
    }

    private String extractCorrelationId() {

        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String correlationId = attributes.getRequest().getHeader("X-Correlation-ID");
        if (correlationId != null) {
            return correlationId;
        }

        return attributes.getRequest().getSession().getId();


    }
}
