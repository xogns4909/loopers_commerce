package com.loopers.handler.impl;

import com.loopers.event.GeneralEnvelopeEvent;
import com.loopers.handler.EventHandler;
import com.loopers.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogHandler implements EventHandler {

    private final AuditLogService auditLogService;

    @Override
    public boolean canHandle(String eventType) {
        return true;
    }

    @Override
    public void handle(GeneralEnvelopeEvent envelope) {
        auditLogService.saveAuditLog(envelope);
    }
}
