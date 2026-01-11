package com.dockey.docs.kafka;

import com.dockey.docs.services.DocumentLineCommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class CommentEventConsumer {

    private static final Logger LOG = LogManager.getLogger(CommentEventConsumer.class.getName());
    private static final String TOPIC = "dockey-comments";
    private static final String GROUP_ID = "docs-service-group";

    @Inject
    private DocumentLineCommentService documentLineCommentService;

    private KafkaConsumer<String, String> consumer;
    private ObjectMapper objectMapper;
    private ExecutorService executor;
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        running = true;
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this::consumeMessages);
        
        LOG.info("Kafka Comment Event Consumer initialized. Topic: {} Group: {} Bootstrap: {}", 
            TOPIC, GROUP_ID, bootstrapServers);
    }

    private void consumeMessages() {
        LOG.info("Started consuming comment events from topic: {}", TOPIC);
        
        while (running) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        processMessage(record.value());
                    } catch (Exception e) {
                        LOG.error("Error processing message: {}", record.value(), e);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    LOG.error("Error polling messages", e);
                }
            }
        }
        
        LOG.info("Stopped consuming comment events");
    }

    private void processMessage(String messageJson) {
        try {
            CommentEventMessage event = objectMapper.readValue(messageJson, CommentEventMessage.class);
            
            LOG.info("Received comment event: type={} docId={} line={} count={}", 
                event.getEventType(), event.getDocId(), event.getLineNumber(), event.getNewCommentCount());

            switch (event.getEventType()) {
                case "COMMENT_ADDED":
                case "COMMENT_DELETED":
                    handleCommentCountChange(event);
                    break;
                    
                case "COMMENT_LIKED":
                case "COMMENT_UNLIKED":
                    // Optional: Track engagement metrics
                    LOG.debug("Comment {} event for commentId: {}", event.getEventType(), event.getCommentId());
                    break;
                    
                default:
                    LOG.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            LOG.error("Failed to process comment event message", e);
        }
    }

    private void handleCommentCountChange(CommentEventMessage event) {
        try {
            // Parse docId as Long (assuming it's the database ID)
            Long documentId = Long.parseLong(event.getDocId());
            Integer lineNumber = event.getLineNumber();
            Integer newCount = event.getNewCommentCount();

            if (newCount != null && newCount > 0) {
                // Update or create line comment count
                documentLineCommentService.updateLineCommentCount(documentId, lineNumber, newCount);
            } else if (newCount != null && newCount == 0) {
                // Delete line comment count when no comments left
                documentLineCommentService.deleteLineCommentCount(documentId, lineNumber);
            }

            LOG.info("Updated line comment count for docId={} line={} count={}", 
                documentId, lineNumber, newCount);
                
        } catch (NumberFormatException e) {
            LOG.error("Invalid docId format: {}", event.getDocId(), e);
        } catch (Exception e) {
            LOG.error("Failed to handle comment count change", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        running = false;
        
        if (consumer != null) {
            consumer.wakeup();
            consumer.close();
            LOG.info("Kafka consumer closed");
        }
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            LOG.info("Consumer executor shutdown");
        }
    }
}
