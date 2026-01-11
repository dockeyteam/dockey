package com.dockey.docs.services;

import com.dockey.docs.kafka.CommentEventMessage;
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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class KafkaCommentConsumer {

    private static final Logger LOG = LogManager.getLogger(KafkaCommentConsumer.class.getName());
    
    private static final String BOOTSTRAP_SERVERS = "kafka:29092";
    private static final String TOPIC = "dockey-comments";
    private static final String GROUP_ID = "docs-service-group";
    
    @Inject
    private DocumentLineCommentService documentLineCommentService;
    
    @PersistenceUnit(unitName = "docs-jpa-unit")
    private EntityManagerFactory emf;
    
    private KafkaConsumer<String, String> consumer;
    private ExecutorService executor;
    private volatile boolean running = false;
    private ObjectMapper objectMapper;

    public KafkaCommentConsumer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        LOG.info("Initializing Kafka Comment Consumer...");
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
        
        running = true;
        executor = Executors.newSingleThreadExecutor();
        executor.submit(this::consumeMessages);
        
        LOG.info("Kafka Comment Consumer initialized and subscribed to topic: {}", TOPIC);
    }

    private void consumeMessages() {
        LOG.info("Starting Kafka consumer loop...");
        
        while (running) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        LOG.info("Received message: key={} value={} partition={} offset={}", 
                            record.key(), record.value(), record.partition(), record.offset());
                        
                        processCommentEvent(record.value());
                        
                    } catch (Exception e) {
                        LOG.error("Error processing Kafka message: {}", record.value(), e);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    LOG.error("Error polling Kafka messages", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        LOG.info("Kafka consumer loop stopped");
    }

    private void processCommentEvent(String eventJson) {
        EntityManager em = null;
        try {
            CommentEventMessage event = objectMapper.readValue(eventJson, CommentEventMessage.class);
            
            Long documentId = Long.valueOf(event.getDocId());
            Integer lineNumber = event.getLineNumber();
            Integer newCount = event.getNewCommentCount();
            
            LOG.info("Processing {} event for docId={} line={} count={}", 
                event.getEventType(), documentId, lineNumber, newCount);
            
            if ("COMMENT_ADDED".equals(event.getEventType()) || "COMMENT_REMOVED".equals(event.getEventType())) {
                em = emf.createEntityManager();
                
                try {
                    em.getTransaction().begin();
                    
                    if (newCount != null && newCount > 0) {
                        // UPSERT using native PostgreSQL syntax
                        int rowsAffected = em.createNativeQuery(
                            "INSERT INTO document_line_comments (document_id, line_number, comment_count) " +
                            "VALUES (?1, ?2, ?3) " +
                            "ON CONFLICT (document_id, line_number) " +
                            "DO UPDATE SET comment_count = EXCLUDED.comment_count")
                            .setParameter(1, documentId)
                            .setParameter(2, lineNumber)
                            .setParameter(3, newCount)
                            .executeUpdate();
                        
                        em.getTransaction().commit();
                        LOG.info("✅ Successfully upserted line comment count for doc={} line={} to count={}, rows affected={}", 
                            documentId, lineNumber, newCount, rowsAffected);
                    } else {
                        // DELETE when count is 0
                        int rowsAffected = em.createNativeQuery(
                            "DELETE FROM document_line_comments WHERE document_id = ?1 AND line_number = ?2")
                            .setParameter(1, documentId)
                            .setParameter(2, lineNumber)
                            .executeUpdate();
                        
                        em.getTransaction().commit();
                        LOG.info("✅ Successfully deleted line comment count for doc={} line={}, rows affected={}", 
                            documentId, lineNumber, rowsAffected);
                    }
                } catch (Exception dbEx) {
                    if (em != null && em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    LOG.error("Database error processing comment event for docId={} line={}", documentId, lineNumber, dbEx);
                    throw dbEx;
                }
            } else {
                LOG.warn("Unknown event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            LOG.error("Failed to process comment event: {}", eventJson, e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @PreDestroy
    public void destroy() {
        LOG.info("Shutting down Kafka Comment Consumer...");
        running = false;
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (consumer != null) {
            consumer.close();
        }
        
        LOG.info("Kafka Comment Consumer shut down");
    }
}
