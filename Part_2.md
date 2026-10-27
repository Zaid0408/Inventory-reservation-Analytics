# **PART 2 — Exact RabbitMQ Architecture \+ Queue Configuration**

This is the **heart of the project**.

We are intentionally making RabbitMQ do more work because you wanted a **RabbitMQ-centric design without Redis**.

Goal:

* Heavy RabbitMQ usage ✅  
* Beginner-manageable ✅  
* Real-world concepts ✅  
* Strong resume impact ✅

---

# **1\. Architecture Philosophy**

We’ll use:

### **Exchanges**

For routing events.

### **Queues**

For processing.

### **TTL Queue**

For reservation expiry.

### **DLQ**

For permanent failures.

### **Retry Queue**

For temporary failures.

This gives you **4 RabbitMQ concepts in one project**.

---

# **2\. RabbitMQ High-Level Flow**

Here’s the exact system flow:

               ┌────────────────────┐  
                │   Order Service    │  
                └─────────┬──────────┘  
                          │  
                   order.created  
                          │  
                          ▼  
              ┌─────────────────────┐  
              │  order.exchange     │  
              └─────────┬───────────┘  
                        │  
                        ▼  
              ┌─────────────────────┐  
              │ order.created.queue │  
              └─────────┬───────────┘  
                        │  
                        ▼  
              ┌─────────────────────┐  
              │ Inventory Service   │  
              └─────────┬───────────┘  
                        │  
             reserve success/failure  
                        │  
                        ▼  
            reservation.ttl.exchange  
                        │  
                        ▼  
          reservation.ttl.queue (5 min)  
                        │  
                  TTL expires  
                        │  
                        ▼  
            reservation.expired.queue  
                        │  
                        ▼  
              release reserved stock

---

# **3\. RabbitMQ Components**

We’ll use **3 exchanges** and **6 queues**.

Sounds large, but it’s actually manageable.

---

# **EXCHANGES**

---

## **1\. Order Exchange**

### **Name**

order.exchange

### **Type**

TopicExchange

Purpose:  
Routes all order-related events.

Routing keys:

order.created  
order.confirmed  
order.cancelled

---

## **2\. Reservation TTL Exchange**

### **Name**

reservation.ttl.exchange

Purpose:  
Handles delayed reservation expiry.

This is how we replace Redis TTL.

---

## **3\. Dead Letter Exchange (DLX)**

### **Name**

dead.letter.exchange

Purpose:  
Handles:

* expired reservations  
* retries  
* permanent failures

---

# **QUEUES**

---

## **Queue 1 — Order Created Queue**

### **Name**

order.created.queue

Consumes:

order.created

Used by:  
Inventory Service

Responsibility:  
Reserve inventory.

---

## **Queue 2 — Order Confirm Queue**

### **Name**

order.confirmed.queue

Consumes:

order.confirmed

Responsibility:  
Finalize reservation.

---

## **Queue 3 — Order Cancel Queue**

### **Name**

order.cancelled.queue

Consumes:

order.cancelled

Responsibility:  
Release stock.

---

## **Queue 4 — Reservation TTL Queue 🔥**

### **Name**

reservation.ttl.queue

Purpose:  
Delayed reservation expiry.

This queue waits:

5 minutes

Then message expires.

Important:  
Nobody consumes from this queue.

It only exists for delay.

---

## **Queue 5 — Reservation Expired Queue**

### **Name**

reservation.expired.queue

Purpose:  
Process expired reservations.

Inventory service listens here.

When consumed:

release stock  
status → EXPIRED

---

## **Queue 6 — Dead Letter Queue (DLQ)**

### **Name**

dead.letter.queue

Purpose:  
Permanent failures.

Example:

3 retries exhausted

You can inspect failed messages.

Very good interview talking point.

---

# **4\. Exact Event Flow**

## **FLOW 1 — Order Creation**

User:

POST /orders

Order service:

1. Save order

CREATED

2. Publish:

Exchange:

order.exchange

Routing key:

order.created

Payload:

{  
  "orderId": 101,  
  "productId": 1,  
  "quantity": 2  
}

---

Inventory service:

Consumes:

order.created.queue

---

# **FLOW 2 — Successful Reservation**

Inventory service:

### **Step 1**

Lock product row

### **Step 2**

Check stock

### **Step 3**

Reserve inventory

Update DB:

available\_stock \-= quantity  
reserved\_stock \+= quantity

### **Step 4**

Order:

CREATED → RESERVED

### **Step 5 🔥**

Publish delayed expiry event.

Exchange:

reservation.ttl.exchange

Routing key:

reservation.expiry

Payload:

{  
  "orderId": 101  
}

This enters:

reservation.ttl.queue

Waits:

5 minutes

---

# **FLOW 3 — Order Confirmation**

User:

POST /orders/{id}/confirm

Order service publishes:

order.confirmed

Inventory service:

Consumes:

order.confirmed.queue

Updates:

reserved\_stock \-= qty

Order:

RESERVED → CONFIRMED

Done.

---

# **FLOW 4 — Reservation Expiry 🔥**

User never confirms.

TTL expires.

RabbitMQ automatically moves message.

Flow:

reservation.ttl.queue  
        ↓ TTL  
dead.letter.exchange  
        ↓  
reservation.expired.queue

Inventory Service consumes:

Checks:

status \== RESERVED

If yes:

Release inventory:

available\_stock \+= qty  
reserved\_stock \-= qty

Update:

RESERVED → EXPIRED

---

# **FLOW 5 — Retry Mechanism**

Temporary failure:

Example:

DB unavailable

Consumer throws exception.

RabbitMQ requeues.

Retry count:

3

After limit:

Move:

dead.letter.queue

---

# **5\. Exact RabbitMQ Diagram**

Here is the final architecture.

                     ORDER SERVICE  
                             │  
                             ▼  
                   ┌─────────────────┐  
                   │ order.exchange  │  
                   └─────────────────┘  
                       │     │     │  
                       │     │     │  
                       ▼     ▼     ▼  
         order.created.queue  
         order.confirmed.queue  
         order.cancelled.queue  
                       │  
                       ▼  
                INVENTORY SERVICE  
                       │  
          reserve inventory success  
                       │  
                       ▼  
         reservation.ttl.exchange  
                       │  
                       ▼  
          reservation.ttl.queue  
                  (TTL \= 5 min)  
                       │  
                   expires  
                       ▼  
           dead.letter.exchange  
                       │  
             ┌─────────┴─────────┐  
             ▼                   ▼  
reservation.expired.queue   dead.letter.queue  
             │  
             ▼  
      release stock

---

# **6\. Queue Configuration Summary**

| Queue | Purpose |
| ----- | ----- |
| order.created.queue | reserve inventory |
| order.confirmed.queue | finalize reservation |
| order.cancelled.queue | release stock |
| reservation.ttl.queue | delayed expiry |
| reservation.expired.queue | expire reservations |
| dead.letter.queue | failed events |

---

# **7\. Why This Design Is Strong**

You’re showing:

### **Async Processing**

RabbitMQ core usage.

### **Delayed Processing**

TTL queue.

### **Failure Recovery**

DLQ \+ retry.

### **Event-driven Thinking**

Order → reservation lifecycle.

### **Consistency**

DB locking.

This is **significantly stronger than CRUD projects** for SDE-1.

---

### **PART 3 next:**

I’ll give:

✅ Spring Boot config  
✅ Queue beans  
✅ Exchange beans  
✅ `@RabbitListener` setup  
✅ Producer config  
✅ Retry config  
✅ DB schema  
✅ `SELECT FOR UPDATE` locking queries (important)  
✅ Exact transactional flow

This is where implementation becomes real.

# **Correction 1 — Use DirectExchange, not TopicExchange**

Earlier I said `TopicExchange`.

For this project:

### **Better choice:**

DirectExchange

Why?

Your routing keys are exact:

order.created  
order.confirmed  
order.cancelled

No wildcard matching needed.

Cleaner for SDE-1.

---

# **Correction 2 — Retry handling**

I earlier oversimplified retry.

RabbitMQ **does not magically track retry count**.

If we keep native RabbitMQ only (your preference), simplest approach is:

### **Version 1 (recommended)**

Use:

spring.rabbitmq.listener.simple.default-requeue-rejected=true

RabbitMQ redelivers automatically.

If still failing:  
 → goes to DLQ.

This is beginner-friendly.
