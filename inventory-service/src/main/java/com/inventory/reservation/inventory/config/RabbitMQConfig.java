package com.inventory.reservation.inventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
/*
Concept:

@Bean = Creates objects managed by Spring
Direct Exchange type= Message router that sends messages to a single queue
Queue = Message storage
Binding = Connects queue to exchange
An exchange is a message receiver in RabbitMQ, and a routing key is the delivery address attached to the message
EX: 
The Exchange is the main local post office.
The Routing Key is the address written on an envelope (e.g., "123 Main St").
The Queue is the individual mailbox or P.O. Box.

How They Work Together:
Exchange receives the message: A producer sends a message to the exchange along with a routing key.
Exchange follows the rules: Depending on its type, the exchange looks at the routing key to decide where to deliver the message.
Message is routed: The message is placed into the matching queue(s) to wait for a consumer to pick it up

*/
@Configuration
public class RabbitMQConfig {

    // -------------------------------------------------------------------------
    // order.created.queue — order-service publishes; InventoryConsumer creates + reserves
    // Exchange: order.exchange | Routing key: order.created
    // -------------------------------------------------------------------------

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QueueConstants.ORDER_CREATED_QUEUE)
            .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(QueueConstants.DEAD_LETTER_ROUTING_KEY)  // "dead.letter"
            .build();
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(QueueConstants.ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(QueueConstants.ORDER_CREATED_ROUTING_KEY);
    }

    // -------------------------------------------------------------------------
    // reservation.ttl.queue — delay timer after reserve; no consumer (Step 7)
    // Exchange: reservation.ttl.exchange | Routing key: reservation.expiry
    // On TTL: dead-letter to dead.letter.exchange with key reservation.expired
    // -------------------------------------------------------------------------

    @Bean
    public Queue reservationTtlQueue(@Value("${app.reservation.ttl-ms}") int reservationTtlMs) {
        return QueueBuilder.durable(QueueConstants.RESERVATION_TTL_QUEUE)
                .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(QueueConstants.RESERVATION_EXPIRED_ROUTING_KEY)
                .ttl(reservationTtlMs)
                .build();
    }

    @Bean
    public DirectExchange reservationTtlExchange() {
        return new DirectExchange(QueueConstants.RESERVATION_TTL_EXCHANGE);
    }

    @Bean
    public Binding reservationTtlBinding(Queue reservationTtlQueue, DirectExchange reservationTtlExchange) {
        return BindingBuilder.bind(reservationTtlQueue)
                .to(reservationTtlExchange)
                .with(QueueConstants.RESERVATION_TTL_ROUTING_KEY);
    }

    // -------------------------------------------------------------------------
    // reservation.expired.queue — TTL expired; InventoryConsumer releases stock (Step 8)
    // Exchange: dead.letter.exchange | Routing key: reservation.expired
    // -------------------------------------------------------------------------

    @Bean
    public Queue reservationExpiredQueue() {
        return QueueBuilder.durable(QueueConstants.RESERVATION_EXPIRED_QUEUE)
            .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(QueueConstants.DEAD_LETTER_ROUTING_KEY)
            .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(QueueConstants.DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding reservationExpiredBinding(Queue reservationExpiredQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(reservationExpiredQueue)
                .to(deadLetterExchange)
                .with(QueueConstants.RESERVATION_EXPIRED_ROUTING_KEY);
    }

    // -------------------------------------------------------------------------
    // dead.letter.queue — permanent failures after retries (later steps)
    // Exchange: dead.letter.exchange (same bean as above) | Routing key: dead.letter
    // -------------------------------------------------------------------------

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QueueConstants.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(QueueConstants.DEAD_LETTER_ROUTING_KEY);
    }

    // -------------------------------------------------------------------------
    // order.confirmed.queue — order-service confirms; InventoryConsumer confirms the order
    // Exchange: order.exchange | Routing key: order.confirmed
    // -------------------------------------------------------------------------
    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(QueueConstants.ORDER_CONFIRMED_QUEUE)
            .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(QueueConstants.DEAD_LETTER_ROUTING_KEY)
            .build();
    }

    @Bean
    public DirectExchange orderConfirmedExchange() {
        return new DirectExchange(QueueConstants.ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderConfirmedBinding(Queue orderConfirmedQueue, DirectExchange orderConfirmedExchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(orderConfirmedExchange).with(QueueConstants.ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public  Queue orderCancelledQueue()
    {
        return QueueBuilder.durable(QueueConstants.ORDER_CANCELLED_QUEUE)
            .deadLetterExchange(QueueConstants.DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey(QueueConstants.DEAD_LETTER_ROUTING_KEY)
            .build();
    }

    @Bean
    public DirectExchange orderCancelledExchange() {
        return new DirectExchange(QueueConstants.ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, DirectExchange orderCancelledExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderCancelledExchange).with(QueueConstants.ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
    /*
    The tool used to receive messages, configured to automatically read the incoming JSON back into Java.
    This is used to receive messages from the order service.
    */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter, SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory); // will read the retries, max attempts, etc from the application.yml file.
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    /*
    1. By calling configurer.configure(factory, connectionFactory) first, Spring Boot reads your listener.simple.retry.max-attempts: 3 and listener.simple.default-requeue-rejected: false from the application.yml and injects them directly into the container factory.
    2. When a listener throws an exception now, RabbitMQ will attempt local retries up to 3 times.
    3. If it still fails after 3 attempts, it respects default-requeue-rejected: false. Instead of sending the message back to the queue for an infinite loop, it will either drop the message or send it to a Dead Letter Exchange (DLX) and routing key.
    */

}
