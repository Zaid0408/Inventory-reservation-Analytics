package com.inventory.reservation.order.config;

public class QueueConstants {
    public static final String ORDER_EXCHANGE= "order.exchange";
    public static final String ORDER_CREATED_QUEUE= "order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY= "order.created";
    public static final String ORDER_CONFIRMED_ROUTING_KEY= "order.confirmed";
    public static final String ORDER_CANCELLED_ROUTING_KEY= "order.cancelled";
    public static final String RESERVATION_TTL_ROUTING_KEY= "reservation.ttl";
    public static final String RESERVATION_EXPIRED_ROUTING_KEY= "reservation.expired";
    public static final String DEAD_LETTER_ROUTING_KEY= "dead.letter";
    public static final String RESERVATION_TTL_EXCHANGE= "reservation.ttl.exchange";
    public static final String RESERVATION_EXPIRED_EXCHANGE= "reservation.expired.exchange";
    public static final String DEAD_LETTER_EXCHANGE= "dead.letter.exchange";
}
