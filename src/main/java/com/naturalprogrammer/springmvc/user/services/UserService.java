package com.naturalprogrammer.springmvc.user.services;

import com.naturalprogrammer.springmvc.user.domain.MyUser;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public UserResource toResponse(MyUser user, String token) {

        return new UserResource(
                user.getId().value().toString(),
                user.getEmail().value(),
                user.getDisplayName().value(),
                user.getLocale().toLanguageTag(),
                token
        );
    }
}
