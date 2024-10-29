package com.example.coffee_shop_app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByUserOrderByOrderDateDesc(User user);


}
