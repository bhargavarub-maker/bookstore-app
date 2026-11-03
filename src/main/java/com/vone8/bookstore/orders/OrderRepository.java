package com.vone8.bookstore.orders;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailIgnoreCaseOrderByIdAsc(String customerEmail);
}
