package com.inventory.reservation.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
    
    private final String orderCreatedQueue = QueueConstants.ORDER_CREATED_QUEUE;
    // A buffer or storage box that holds messages until a consumer application (like an Inventory service) reads and processes them.
    @Bean
    public Queue orderCreatedQueue()
    {
        return new Queue(orderCreatedQueue);
    }
    // The entry point for message producers. In RabbitMQ, producers never send messages directly to a queue.
    //  Instead, they publish messages to an Exchange, which acts like a mail router deciding where messages should go
    // A Direct Exchange routes messages based on an exact match between a message's routing key and a binding key.
    @Bean
    public DirectExchange orderExchange()
    {
        return new DirectExchange(QueueConstants.ORDER_EXCHANGE);
    }
    // The physical link or relationship between an Exchange and a Queue. It tells the exchange: "If you receive a message with key X, send it to Queue Y."
    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange)
    {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with(QueueConstants.ORDER_CREATED_ROUTING_KEY);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    // Spring's high-level helper class used by your application to publish and receive messages conveniently
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
