package com.inventory.reservation.inventory.enums;

public class ReservationEnums {
    public enum ReservationStatus {
        RESERVED("RESERVED"),
        EXPIRED("EXPIRED"),
        CANCELLED("CANCELLED"),
        FAILED("FAILED");

        private final String value;

        ReservationStatus(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }
        
    }
}
