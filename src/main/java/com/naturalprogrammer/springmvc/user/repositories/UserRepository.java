package com.naturalprogrammer.springmvc.user.repositories;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<MyUser, UUID> {

    boolean existsByEmail(String email);
    Optional<MyUser> findByEmail(String username);
}
