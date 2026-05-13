package com.capg.pharma.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Notification Service (consumer side).
 *
 * <p>Declares the exchange, queue, and binding that the notification listener
 * uses to receive ORDER_PLACED and other notification events from the order-service.</p>
 *
 * <p>Exchange: {@code pharmacy.exchange} (TopicExchange)<br>
 * Queue: {@code notification.queue} (durable)<br>
 * Routing key: {@code notification.send}</p>
 */
@Configuration
public class RabbitMQConfig {

    /** Name of the topic exchange used for all pharmacy async events. */
    public static final String EXCHANGE = "pharmacy.exchange";

    /** Name of the durable queue that receives notification events. */
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    /** Routing key that binds the queue to the exchange. */
    public static final String ROUTING_KEY = "notification.send";

    /**
     * Declares the topic exchange.
     *
     * @return a durable {@link TopicExchange}
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    /**
     * Declares the notification queue as durable (survives broker restarts).
     *
     * @return a durable {@link Queue}
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /**
     * Binds the notification queue to the exchange with the routing key.
     *
     * @param notificationQueue the queue to bind
     * @param exchange          the exchange to bind to
     * @return the configured {@link Binding}
     */
    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * Configures JSON message deserialization for incoming RabbitMQ messages.
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
