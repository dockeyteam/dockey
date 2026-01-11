package com.dockey.comments.producers;

import com.dockey.comments.config.CommentEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.util.Properties;

@ApplicationScoped
public class KafkaCommentProducer {

    private static final Logger LOG = LogManager.getLogger(KafkaCommentProducer.class.getName());
    private static final String TOPIC = "dockey-comments";

    private Producer<String, String> producer;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        producer = new KafkaProducer<>(props);
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        LOG.info("Kafka Comment Producer initialized with bootstrap servers: " + bootstrapServers);
    }

    public void sendCommentEvent(CommentEventMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            // Use docId as key to ensure all events for same doc go to same partition
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message.getDocId(), jsonMessage);
            
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOG.error("Error sending comment event to Kafka", exception);
                } else {
                    LOG.info("Comment event sent: type={} docId={} line={} topic={} partition={} offset={}", 
                        message.getEventType(), message.getDocId(), message.getLineNumber(),
                        metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            
        } catch (Exception e) {
            LOG.error("Failed to serialize or send comment event", e);
            throw new RuntimeException("Failed to send comment event to Kafka", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.close();
            LOG.info("Kafka Comment Producer closed");
        }
    }
}
