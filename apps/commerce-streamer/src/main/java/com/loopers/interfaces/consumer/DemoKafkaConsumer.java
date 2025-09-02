package com.loopers.interfaces.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoKafkaConsumer {
    
    @KafkaListener(
        topics = "${demo-kafka.test.topic-name}",
        containerFactory = "BATCH_LISTENER_DEFAULT"
    )
    public void demoListener(List<ConsumerRecord<Object, Object>> messages, Acknowledgment acknowledgment) {
        System.out.println(messages);
        acknowledgment.acknowledge(); // manual ack
    }
}
