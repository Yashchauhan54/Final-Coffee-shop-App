package com.example.coffee_shop_app;

import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
