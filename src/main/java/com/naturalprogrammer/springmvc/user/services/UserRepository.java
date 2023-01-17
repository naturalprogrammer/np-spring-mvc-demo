package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.domain.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<MyUser, UserId> {
}
