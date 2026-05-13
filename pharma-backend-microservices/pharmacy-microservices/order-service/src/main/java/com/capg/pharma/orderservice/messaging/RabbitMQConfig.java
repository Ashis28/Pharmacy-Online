package com.capg.pharma.orderservice.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Order Service (publisher side).
 *
 * <p>Declares the exchange so it exists even if notification-service hasn't
 * started yet. Both services declaring the same durable exchange is safe —
 * RabbitMQ is idempotent for exchange declarations with identical parameters.</p>
 *
 * <p>Exchange: {@code pharmacy.exchange} (TopicExchange)<br>
 * Routing key: {@code notification.send}</p>
 */
@Configuration
public class RabbitMQConfig {

    /** Name of the topic exchange used for all pharmacy async events. */
    public static final String EXCHANGE = "pharmacy.exchange";

    /** Routing key for notification messages. */
    public static final String ROUTING_KEY = "notification.send";

    /**
     * Declares the topic exchange on the broker.
     * Declaring it here ensures it exists before the first publish attempt,
     * regardless of whether notification-service has started.
     *
     * @return a durable {@link TopicExchange}
     */
    @Bean
    public TopicExchange pharmacyExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /**
     * Configures JSON message serialization for RabbitMQ messages.
     *
     * @return a {@link Jackson2JsonMessageConverter} bean
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate with JSON message conversion.
     *
     * @param connectionFactory the RabbitMQ connection factory
     * @return a configured {@link RabbitTemplate}
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
