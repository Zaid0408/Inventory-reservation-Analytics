package com.inventory.reservation.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.reservation.order.entity.Order;

/*
The @Repository annotation in Spring Boot is a class-level marker indicating that the annotated class or interface 
directly handles database interactions and data persistence operations. 
It isolates your database logic from the business logic by acting as a clean bridge between your services and your storage mechanism.

This is important because it allows you to keep your business logic clean and focused on your business needs, 
while the repository handles the database interactions.
*/
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
}

/*
By extending JpaRepository<Order, Long>, your interface automatically inherits over a dozen methods without writing a single line of code, including:
save(T entity) (Used for both inserting new records and updating existing ones)
findById(ID id) (Finds a record by its primary key)
findAll() (Retrieves all records from the table)
deleteById(ID id) (Deletes a record by its primary key)
count() (Returns the total number of records)
existsById(ID id) (Checks if a record exists by its primary key)
findAll(Sort sort) (Retrieves all records from the table with sorting)
findAll(Example<T> example) (Finds all records matching the example)
findAll(Example<T> example, Sort sort) (Finds all records matching the example with sorting)
findAll(Pageable pageable) (Retrieves all records from the table with pagination)
findAll(Example<T> example, Pageable pageable) (Finds all records matching the example with pagination)
*/
