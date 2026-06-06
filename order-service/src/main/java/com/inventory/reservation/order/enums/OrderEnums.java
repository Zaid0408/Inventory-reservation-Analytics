package com.inventory.reservation.order.enums;

public class OrderEnums { // order status enum 
    
    public enum OrderStatus {
        CREATED("CREATED"),
        RESERVED("RESERVED"),
        CONFIRMED("CONFIRMED"),
        CANCELLED("CANCELLED"),
        FAILED("FAILED"),
        EXPIRED("EXPIRED");

        private final String value;

        OrderStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
