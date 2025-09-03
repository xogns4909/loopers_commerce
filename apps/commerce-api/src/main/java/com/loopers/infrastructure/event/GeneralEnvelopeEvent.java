package com.loopers.infrastructure.event;


public record GeneralEnvelopeEvent(
    String messageId,
    String type,
    String schemaVersion,
    String occurredAt,
    String source,
    String correlationId,
    Object payload
) {

    public static GeneralEnvelopeEvent from(Envelope<?> envelope) {
        return new GeneralEnvelopeEvent(
            envelope.messageId(),
            envelope.type(),
            "v1",
            envelope.occurredAt().toString(),
            envelope.source(),
            envelope.correlationId(),
            envelope.payload()
        );
    }
}
