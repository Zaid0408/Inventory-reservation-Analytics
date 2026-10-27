# **PART 3 — Spring Boot Config \+ DB Schema \+ Locking Queries**

This is the implementation blueprint.

We’ll keep it **clean, beginner-friendly, and production-ish**.

---

# **1\. Tech Stack**

### **Backend**

* Java 21 (or 17\)  
* Spring Boot 3

### **Database**

* PostgreSQL

### **Message Broker**

* RabbitMQ

### **ORM**

* Spring Data JPA

### **Build Tool**

* Maven

---

# **2\. Maven Dependencies**

Add these:

\<dependencies\>

    \<\!-- REST APIs \--\>  
    \<dependency\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-web\</artifactId\>  
    \</dependency\>

    \<\!-- JPA \--\>  
    \<dependency\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-data-jpa\</artifactId\>  
    \</dependency\>

    \<\!-- PostgreSQL \--\>  
    \<dependency\>  
        \<groupId\>org.postgresql\</groupId\>  
        \<artifactId\>postgresql\</artifactId\>  
    \</dependency\>

    \<\!-- RabbitMQ \--\>  
    \<dependency\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-amqp\</artifactId\>  
    \</dependency\>

    \<\!-- Validation \--\>  
    \<dependency\>  
        \<groupId\>org.springframework.boot\</groupId\>  
        \<artifactId\>spring-boot-starter-validation\</artifactId\>  
    \</dependency\>

    \<\!-- Lombok \--\>  
    \<dependency\>  
        \<groupId\>org.projectlombok\</groupId\>  
        \<artifactId\>lombok\</artifactId\>  
        \<optional\>true\</optional\>  
    \</dependency\>

\</dependencies\>

---

# **3\. Folder Structure**

### **Order Service**

order-service  
│  
├── controller  
│   └── OrderController  
│  
├── service  
│   └── OrderService  
│  
├── producer  
│   └── OrderEventProducer  
│  
├── entity  
│   └── Order  
│  
├── repository  
│   └── OrderRepository  
│  
├── dto  
│   ├── CreateOrderRequest  
│   └── OrderEvent  
│  
├── config  
│   └── RabbitMQConfig  
│  
└── enums  
    └── OrderStatus

---

### **Inventory Service**

inventory-service  
│  
├── consumer  
│   └── InventoryConsumer  
│  
├── service  
│   └── InventoryService  
│  
├── entity  
│   ├── Product  
│   └── InventoryReservation  
│  
├── repository  
│   ├── ProductRepository  
│   └── ReservationRepository  
│  
├── dto  
│   └── OrderEvent  
│  
├── config  
│   └── RabbitMQConfig  
│  
└── enums  
    └── ReservationStatus

---

# **4\. RabbitMQ Configuration**

## **Queue Names**

public class QueueConstants {

    public static final String ORDER\_EXCHANGE \=  
            "order.exchange";

    public static final String DEAD\_LETTER\_EXCHANGE \=  
            "dead.letter.exchange";

    public static final String ORDER\_CREATED\_QUEUE \=  
            "order.created.queue";

    public static final String ORDER\_CONFIRMED\_QUEUE \=  
            "order.confirmed.queue";

    public static final String ORDER\_CANCELLED\_QUEUE \=  
            "order.cancelled.queue";

    public static final String RESERVATION\_TTL\_QUEUE \=  
            "reservation.ttl.queue";

    public static final String RESERVATION\_EXPIRED\_QUEUE \=  
            "reservation.expired.queue";

    public static final String DEAD\_LETTER\_QUEUE \=  
            "dead.letter.queue";  
}

---

## **RabbitMQ Config**

@Configuration  
public class RabbitMQConfig {

    @Bean  
    public DirectExchange orderExchange() {  
        return new DirectExchange("order.exchange");  
    }

    @Bean  
    public DirectExchange deadLetterExchange() {  
        return new DirectExchange("dead.letter.exchange");  
    }

    @Bean  
    public Queue orderCreatedQueue() {  
        return QueueBuilder  
                .durable("order.created.queue")  
                .build();  
    }

    @Bean  
    public Queue orderConfirmedQueue() {  
        return QueueBuilder  
                .durable("order.confirmed.queue")  
                .build();  
    }

    @Bean  
    public Queue orderCancelledQueue() {  
        return QueueBuilder  
                .durable("order.cancelled.queue")  
                .build();  
    }

    @Bean  
    public Queue reservationTTLQueue() {

        return QueueBuilder  
                .durable("reservation.ttl.queue")  
                .deadLetterExchange(  
                        "dead.letter.exchange")  
                .deadLetterRoutingKey(  
                        "reservation.expired")  
                .ttl(300000)  
                .build();  
    }

    @Bean  
    public Queue reservationExpiredQueue() {  
        return QueueBuilder  
                .durable(  
                  "reservation.expired.queue")  
                .build();  
    }

    @Bean  
    public Queue deadLetterQueue() {  
        return QueueBuilder  
                .durable("dead.letter.queue")  
                .build();  
    }

    @Bean  
    public Binding orderCreatedBinding(  
            Queue orderCreatedQueue,  
            DirectExchange orderExchange  
    ) {  
        return BindingBuilder  
                .bind(orderCreatedQueue)  
                .to(orderExchange)  
                .with("order.created");  
    }

}

(Repeat bindings similarly for confirmed/cancelled)

---

# **5\. Producer Example**

### **OrderEventProducer**

@Service  
@RequiredArgsConstructor  
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(  
            OrderEvent event  
    ) {

        rabbitTemplate.convertAndSend(  
                "order.exchange",  
                "order.created",  
                event  
        );  
    }  
}

---

# **6\. Consumer Example**

### **Inventory Consumer**

@Component  
@RequiredArgsConstructor  
public class InventoryConsumer {

    private final InventoryService  
            inventoryService;

    @RabbitListener(  
            queues \= "order.created.queue"  
    )  
    public void processOrder(  
            OrderEvent event  
    ) {

        inventoryService  
                .reserveInventory(event);  
    }  
}

---

# **7\. Database Schema**

## **Product Table**

CREATE TABLE products (  
    id BIGSERIAL PRIMARY KEY,  
    name VARCHAR(255),  
    available\_stock INT NOT NULL,  
    reserved\_stock INT NOT NULL DEFAULT 0  
);

---

## **Orders Table**

CREATE TABLE orders (  
    id BIGSERIAL PRIMARY KEY,  
    product\_id BIGINT,  
    quantity INT,  
    status VARCHAR(50),  
    created\_at TIMESTAMP  
);

---

## **Reservation Table**

CREATE TABLE inventory\_reservations (  
    id BIGSERIAL PRIMARY KEY,  
    order\_id BIGINT UNIQUE,  
    product\_id BIGINT,  
    quantity INT,  
    status VARCHAR(50),  
    expires\_at TIMESTAMP  
);

---

# **8\. Entities**

## **Product Entity**

@Entity  
@Table(name \= "products")  
@Getter  
@Setter  
public class Product {

    @Id  
    @GeneratedValue(strategy \=  
            GenerationType.IDENTITY)  
    private Long id;

    private String name;

    private Integer availableStock;

    private Integer reservedStock;  
}

---

## **Order Status Enum**

public enum OrderStatus {

    CREATED,  
    RESERVED,  
    CONFIRMED,  
    CANCELLED,  
    FAILED,  
    EXPIRED  
}

---

# **9\. THE IMPORTANT PART**

# **Prevent Overselling (DB Locking)**

This is your strongest backend concept.

Without locking:

Bad scenario:

Stock \= 1

Thread A reads stock \= 1  
Thread B reads stock \= 1

Both update

Oversold ❌

---

## **Correct Approach**

### **Use:**

SELECT \*  
FROM products  
WHERE id \= ?  
FOR UPDATE

This locks row.

Only ONE transaction proceeds.

---

## **Repository Query**

@Repository  
public interface ProductRepository  
        extends JpaRepository\<Product, Long\> {

    @Lock(LockModeType.PESSIMISTIC\_WRITE)  
    @Query("""  
           SELECT p  
           FROM Product p  
           WHERE p.id \= :id  
           """)  
    Optional\<Product\>  
    findProductForUpdate(Long id);  
}

This is basically:

FOR UPDATE

under the hood.

---

# **10\. Reservation Logic**

## **Inventory Service**

@Service  
@RequiredArgsConstructor  
public class InventoryService {

    private final ProductRepository  
            productRepository;

    @Transactional  
    public void reserveInventory(  
            OrderEvent event  
    ) {

        Product product \=  
                productRepository  
                .findProductForUpdate(  
                        event.getProductId()  
                )  
                .orElseThrow();

        if(product.getAvailableStock()  
                \< event.getQuantity()) {

            throw new RuntimeException(  
                    "Insufficient stock"  
            );  
        }

        product.setAvailableStock(  
                product.getAvailableStock()  
                        \- event.getQuantity()  
        );

        product.setReservedStock(  
                product.getReservedStock()  
                        \+ event.getQuantity()  
        );

        productRepository.save(product);  
    }  
}

---

# **11\. Expiry Consumer**

@RabbitListener(  
        queues \=  
        "reservation.expired.queue"  
)  
@Transactional  
public void expireReservation(  
        ReservationEvent event  
) {

    Reservation reservation \=  
            reservationRepository  
                    .findByOrderId(  
                            event.getOrderId()  
                    );

    if(reservation.getStatus()  
            \!= RESERVED) {  
        return;  
    }

    Product product \=  
            productRepository  
            .findProductForUpdate(  
                    reservation.getProductId()  
            )  
            .orElseThrow();

    product.setAvailableStock(  
            product.getAvailableStock()  
            \+ reservation.getQuantity()  
    );

    product.setReservedStock(  
            product.getReservedStock()  
            \- reservation.getQuantity()  
    );

    reservation.setStatus(EXPIRED);  
}

---

# **12\. application.yml**

spring:  
  datasource:  
    url: jdbc:postgresql://localhost:5432/inventory\_db  
    username: postgres  
    password: password

  jpa:  
    hibernate:  
      ddl-auto: update  
    show-sql: true

  rabbitmq:  
    host: localhost  
    port: 5672  
    username: guest  
    password: guest

---

# **13\. Docker Compose**

version: '3.8'

services:

  postgres:  
    image: postgres  
    environment:  
      POSTGRES\_DB: inventory\_db  
      POSTGRES\_USER: postgres  
      POSTGRES\_PASSWORD: password

    ports:  
      \- "5432:5432"

  rabbitmq:  
    image: rabbitmq:3-management  
    ports:  
      \- "5672:5672"  
      \- "15672:15672"

RabbitMQ dashboard:

localhost:15672

---

# **14\. Recommended Build Order**

Build exactly in this order:

1. PostgreSQL schema  
2. Create order API  
3. RabbitMQ producer  
4. RabbitMQ consumer  
5. Reserve inventory  
6. DB locking (`PESSIMISTIC_WRITE`)  
7. TTL queue  
8. Expiry flow  
9. Confirm/cancel flow  
10. DLQ testing  
11. Dockerize  
12. Deploy

This order will save you from getting stuck.
