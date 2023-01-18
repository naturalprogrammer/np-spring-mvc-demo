package com.naturalprogrammer.springmvc.user.repositories;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.UserIdClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<MyUser, UserIdClass> {
}
