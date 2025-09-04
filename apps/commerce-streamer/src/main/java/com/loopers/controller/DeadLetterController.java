package com.loopers.controller;

import com.loopers.entity.DeadLetterEvent;
import com.loopers.repository.DeadLetterEventRepository;
import com.loopers.service.DeadLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/dead-letters")
@RequiredArgsConstructor
public class DeadLetterController {

    private final DeadLetterService deadLetterService;


    @GetMapping
    public ResponseEntity<Page<DeadLetterEvent>> getDeadLetterEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DeadLetterEvent> events = deadLetterService.getDeadLetterEvents(pageable);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/by-type/{eventType}")
    public ResponseEntity<Page<DeadLetterEvent>> getDeadLetterEventsByType(
            @PathVariable String eventType,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DeadLetterEvent> events = deadLetterService.getDeadLetterEventsByType(eventType, pageable);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/by-reason/{failureReason}")
    public ResponseEntity<Page<DeadLetterEvent>> getDeadLetterEventsByFailureReason(
            @PathVariable DeadLetterEvent.FailureReason failureReason,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DeadLetterEvent> events = deadLetterService.getDeadLetterEventsByFailureReason(failureReason, pageable);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/{messageId}")
    public ResponseEntity<DeadLetterEvent> getDeadLetterEvent(@PathVariable String messageId) {
        Optional<DeadLetterEvent> event = deadLetterService.getDeadLetterEventByMessageId(messageId);
        return event.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/failures")
    public ResponseEntity<List<DeadLetterEventRepository.DeadLetterEventSummary>> getFailureStats(
            @RequestParam(defaultValue = "24") int hours) {
        List<DeadLetterEventRepository.DeadLetterEventSummary> summary = 
            deadLetterService.getFailureSummary(hours);
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/stats/count")
    public ResponseEntity<Long> getDeadLetterCount(@RequestParam(defaultValue = "24") int hours) {
        long count = deadLetterService.getDeadLetterCount(hours);
        return ResponseEntity.ok(count);
    }


    @PutMapping("/{id}/investigate")
    public ResponseEntity<Void> markAsInvestigating(
            @PathVariable Long id,
            @RequestParam String investigator) {
        deadLetterService.markAsInvestigating(id, investigator);
        return ResponseEntity.ok().build();
    }



}
