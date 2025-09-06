package com.loopers.infrastructure.event;


public record GeneralEnvelopeEvent<T>(
    String messageId,
    String type,
    String schemaVersion,
    String occurredAt,
    String source,
    String correlationId,
    T payload
) {


    public static <T> GeneralEnvelopeEvent<T> from(Envelope<T> envelope) {
        return new GeneralEnvelopeEvent<>(
            envelope.messageId(),
            envelope.type(),
            "v1",
            envelope.occurredAt().toString(),
            envelope.source(),
            envelope.correlationId(),
            envelope.payload()
        );
    }
    


    public GeneralEnvelopeEvent<Object> toUntyped() {
        return new GeneralEnvelopeEvent<>(
            messageId, type, schemaVersion, occurredAt, 
            source, correlationId, (Object) payload
        );
    }
}
