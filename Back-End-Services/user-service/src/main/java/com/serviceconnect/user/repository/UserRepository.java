package com.serviceconnect.user.repository;

import com.serviceconnect.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}