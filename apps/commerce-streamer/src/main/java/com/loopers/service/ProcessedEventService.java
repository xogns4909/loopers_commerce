package com.loopers.service;


import com.loopers.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository repository;


    @Transactional
    public boolean tryStart(String messageId, String eventType, String correlationId) {
        return repository.tryInsertProcessing(messageId, eventType, correlationId);
    }


    @Transactional
    public void markProcessed(String messageId) {
        int updated = repository.markProcessed(messageId, ZonedDateTime.now());
        if (updated == 0) {
            // 이미 PROCESSED 또는 선점 실패 상태일 수 있음 – 필요시 로그
        }
    }


    @Transactional
    public void markFailed(String messageId) {
        int updated = repository.markFailed(messageId, ZonedDateTime.now());
        if (updated == 0) {
            // 이미 PROCESSED 등 – 필요시 로그
        }
    }
}
