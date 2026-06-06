# **PART 1 — PRD \+ System Design**

# **Inventory Reservation System (RabbitMQ-Centric)**

---

# **1\. Product Overview**

## **Project Name**

**Inventory Reservation System**

---

## **One-line Resume Description**

Built a RabbitMQ-driven inventory reservation system to prevent overselling by handling asynchronous order processing, reservation expiry, retries, and failure recovery using Spring Boot and PostgreSQL.

---

# **2\. Problem Statement**

In e-commerce systems, multiple users may try to buy the same product simultaneously.

Example:

Inventory:

iPhone 15 → Stock \= 2

Users:

User A buys 1  
User B buys 1  
User C buys 1

Without proper handling:  
❌ System oversells inventory

Expected behavior:

* First 2 succeed  
* Last request fails gracefully

Additionally:

* Reserved items should expire automatically if payment/order isn't confirmed.  
* Failed processing should retry safely.  
* Inventory consistency must always remain correct.

---

# **3\. Goal of the System**

### **Primary Goal**

Prevent **overselling of inventory** under concurrent requests.

### **Secondary Goals**

* Event-driven architecture using RabbitMQ  
* Automatic reservation expiry  
* Retry failed operations  
* Learn backend system design patterns  
* Production-like SDE-1 backend project

---

# **4\. System Scope**

We intentionally keep scope controlled for a **20-day SDE-1 project**.

---

## **IN SCOPE ✅**

### **Inventory Reservation**

Reserve stock temporarily.

Example:

Stock \= 10

User reserves 2

Available stock \= 8  
Reserved stock \= 2

---

### **Order Processing**

Orders created asynchronously through RabbitMQ.

---

### **Reservation Expiry**

Reservations expire automatically after 5 minutes.

If order not confirmed:

reserved stock → released

---

### **Retry Handling**

Failed message processing retries automatically.

---

### **Dead Letter Queue**

Permanent failures go to DLQ.

---

### **Concurrency Handling**

Prevent overselling using DB locking.

---

### **State Management**

Order lifecycle tracking.

---

## **OUT OF SCOPE ❌**

We intentionally skip:

* Payments  
* Authentication/JWT  
* WebSockets  
* Redis  
* Notifications  
* Kubernetes  
* Distributed transactions  
* Optimistic locking  
* Saga pattern

Reason:  
You’re building an **SDE-1 level project**, not Amazon warehouse infra.

---

# **5\. Functional Requirements**

---

## **FR1 — Create Order**

User places an order.

Input:

{  
  "productId": 1,  
  "quantity": 2  
}

Expected:

1. Order created in DB  
2. Status \= CREATED  
3. Event published to RabbitMQ

---

## **FR2 — Reserve Inventory**

Inventory service consumes event.

Flow:

Check stock.

If enough:

reserve stock  
status \= RESERVED

Else:

status \= FAILED

---

## **FR3 — Reservation Expiry**

Every reservation gets expiry:

5 minutes

After timeout:

If not confirmed:

release stock  
mark EXPIRED

---

## **FR4 — Confirm Order**

User confirms reservation.

Expected:

RESERVED → CONFIRMED

Inventory becomes permanent deduction.

---

## **FR5 — Cancel Order**

User cancels.

Expected:

release reserved inventory

---

## **FR6 — Retry Failed Events**

Temporary failures:

* DB timeout  
* MQ connection issue

Expected:

retry automatically

Max retries:

3

After that:

DLQ

---

# **6\. Non-Functional Requirements**

---

## **Consistency**

Must never oversell inventory.

Even under concurrency.

Example:

Stock \= 1  
5 simultaneous requests

Only:

1 success  
4 failures

---

## **Reliability**

Messages should not be lost.

RabbitMQ persistence enabled.

---

## **Scalability (basic level)**

Inventory service can scale horizontally later.

(You won't implement scaling now.)

---

## **Performance**

Order creation should feel fast.

Reason:  
API only publishes event.

Heavy work is async.

---

# **7\. High-Level User Flow**

---

## **Successful Order**

User creates order  
        ↓  
Order Service stores CREATED  
        ↓  
RabbitMQ event published  
        ↓  
Inventory Service consumes  
        ↓  
Stock available?  
        ↓ YES  
Reserve stock  
        ↓  
Status → RESERVED  
        ↓  
TTL timer starts  
        ↓  
User confirms  
        ↓  
CONFIRMED

---

## **Failed Order**

Create order  
      ↓  
Inventory consume  
      ↓  
No stock  
      ↓  
FAILED

---

## **Expired Reservation**

RESERVED  
      ↓  
5 min timeout  
      ↓  
Release stock  
      ↓  
EXPIRED

---

# **8\. State Machine (VERY IMPORTANT)**

This is one of your strongest interview talking points.

---

## **Order States**

CREATED  
   ↓  
RESERVED  
   ↓  
CONFIRMED

Failure paths:

CREATED → FAILED

RESERVED → EXPIRED

RESERVED → CANCELLED

---

### **Valid transitions**

| Current | Allowed |
| ----- | ----- |
| CREATED | RESERVED, FAILED |
| RESERVED | CONFIRMED, EXPIRED, CANCELLED |
| FAILED | none |
| CONFIRMED | none |
| EXPIRED | none |

---

## **Why this matters**

Interviewer may ask:

Why use State Pattern?

Answer:

Order behavior changes based on lifecycle stage, and invalid transitions should be prevented.

Example:

CONFIRMED → CREATED

Should never happen.

---

# **9\. System Components**

We keep only **2 services**.

---

## **1\. Order Service**

Responsibilities:

### **APIs**

* Create order  
* Confirm order  
* Cancel order  
* Get order status

### **RabbitMQ**

Produces:

order.created  
order.confirmed  
order.cancelled

---

## **2\. Inventory Service**

Responsibilities:

* Consume events  
* Reserve stock  
* Release stock  
* Expiry handling

Consumes:

order.created  
reservation.expired  
order.cancelled

Produces:

inventory.reserved  
inventory.failed

---

## **3\. RabbitMQ (Centerpiece)**

Responsible for:

### **Async communication**

Order → Inventory

### **Delay processing**

Reservation expiry

### **Retry**

### **Failure handling**

RabbitMQ becomes the backbone.

---

## **4\. PostgreSQL**

Single source of truth.

Stores:

* products  
* inventory  
* reservations  
* orders

---

# **10\. APIs (High-Level)**

---

## **Create Order**

POST /orders

Request:

{  
  "productId": 1,  
  "quantity": 2  
}

Response:

{  
  "orderId": 101,  
  "status": "CREATED"  
}

---

## **Confirm Order**

POST /orders/{id}/confirm

Response:

{  
  "status": "CONFIRMED"  
}

---

## **Cancel Order**

POST /orders/{id}/cancel

---

## **Get Order**

GET /orders/{id}

---

# **11\. Edge Cases (Interview Gold)**

---

### **Case 1**

Two users buy last product.

Expected:

only one succeeds

---

### **Case 2**

Reservation expires while user confirms.

Expected:  
DB lock ensures correctness.

---

### **Case 3**

RabbitMQ consumer crashes.

Expected:  
Message redelivery.

---

### **Case 4**

DB temporarily unavailable.

Expected:  
Retry queue.

---

### **Case 5**

Duplicate event delivery.

Expected:  
Idempotency check using orderId.

(We'll implement simple version.)

---

# **12\. Success Criteria**

Project successful if:

✅ No overselling

✅ Reservation expires automatically

✅ RabbitMQ retry works

✅ DLQ works

✅ Concurrency handled

✅ Can explain architecture in interview

---

# **13\. Suggested Folder Structure (High Level)**

inventory-reservation-system

├── order-service  
│   ├── controller  
│   ├── service  
│   ├── producer  
│   ├── entity  
│   ├── repository  
│   └── config

├── inventory-service  
│   ├── consumer  
│   ├── service  
│   ├── strategy  
│   ├── entity  
│   ├── repository  
│   └── config

---

### **What Part 2 will cover**

Next I’ll give:

✅ **Exact RabbitMQ architecture**  
✅ Exchange/Queue names  
✅ TTL queue  
✅ Retry queue  
✅ Dead letter flow  
✅ Exact routing keys  
✅ Diagram of message movement

That’s where the system really becomes solid.

 