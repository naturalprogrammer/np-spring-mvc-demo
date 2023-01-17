package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.services.SignupService;
import com.naturalprogrammer.springmvc.user.services.SignupService.SignupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

import static com.naturalprogrammer.springmvc.common.CommonConstants.ACCEPT_LANGUAGE;
import static com.naturalprogrammer.springmvc.common.CommonConstants.CONTENT_TYPE;

@RestController
@RequiredArgsConstructor
public class SignupController {

    private final SignupService signupService;

    @PostMapping("/users")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest request,
            @RequestHeader(name = ACCEPT_LANGUAGE, required = false) String language) {

        Locale locale = language == null ? Locale.ENGLISH : Locale.forLanguageTag(language);
        return toResponse(signupService.signup(request, locale));
    }

    private ResponseEntity<?> toResponse(SignupResult result) {
        return switch (result) {
            case SignupResult.Success success -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(CONTENT_TYPE, UserResource.CONTENT_TYPE)
                    .body(success.response());
            case SignupResult.ValidationError error -> Problem.toResponse(error.problem());
        };
    }
}
