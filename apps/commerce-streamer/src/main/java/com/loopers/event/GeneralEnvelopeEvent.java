package com.loopers.event;

import com.fasterxml.jackson.databind.JsonNode;

public record GeneralEnvelopeEvent(
    String messageId,
    String type,
    String schemaVersion,
    String occurredAt,
    String source,
    String correlationId,
    JsonNode payload
) {}
