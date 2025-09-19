package com.thefreelancer.microservices.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thefreelancer.microservices.notification.event.ProposalSubmittedEvent;
import com.thefreelancer.microservices.notification.event.ProposalAcceptedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-service-group}")
    private String groupId;
    
    private final ObjectMapper objectMapper;
    
    public KafkaConsumerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ConsumerFactory<String, ProposalSubmittedEvent> proposalSubmittedConsumerFactory() {
        return createConsumerFactory(ProposalSubmittedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, ProposalAcceptedEvent> proposalAcceptedConsumerFactory() {
        return createConsumerFactory(ProposalAcceptedEvent.class);
    }


    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> eventClass) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Changed from latest
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Process fewer records at once
        configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 40000);

        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(eventClass, objectMapper);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeHeaders(false);

        ErrorHandlingDeserializer<T> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalSubmittedEvent> proposalSubmittedKafkaListenerContainerFactory() {
        return createListenerContainerFactory(proposalSubmittedConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProposalAcceptedEvent> proposalAcceptedKafkaListenerContainerFactory() {
        return createListenerContainerFactory(proposalAcceptedConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createStringConsumerFactory());
        
        // Configure error handling
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, exception) -> {
                // Log and skip the problematic message
                System.err.println("Skipping message due to deserialization error: " + exception.getMessage());
                System.err.println("Record: " + record);
            },
            new FixedBackOff(1000L, 3) // 3 retries with 1 second delay
        );
        
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }

    private ConsumerFactory<String, String> createStringConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Changed from latest
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); // Process fewer records at once
        configProps.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 40000);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createListenerContainerFactory(ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        
        // Configure error handling
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            (record, exception) -> {
                // Log and skip the problematic message
                System.err.println("Skipping message due to deserialization error: " + exception.getMessage());
                System.err.println("Record: " + record);
            },
            new FixedBackOff(1000L, 3) // 3 retries with 1 second delay
        );
        
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        return factory;
    }
}