package com.naturalprogrammer.springmvc.user.features.get_by_email;

import com.naturalprogrammer.springmvc.user.repositories.UserRepository;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import com.naturalprogrammer.springmvc.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class UsersGetter {

    private final UserRepository userRepository;
    private final UserService userService;

    public List<UserResource> getBy(String email) {

        var users = userRepository
                .findByEmail(email)
                .stream()
                .map(userService::toResource)
                .toList();

        log.info("Got users by email {}: {}", email, users);
        return users;
    }
}
