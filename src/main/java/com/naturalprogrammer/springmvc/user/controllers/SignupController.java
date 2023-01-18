package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.services.SignupService;
import com.naturalprogrammer.springmvc.user.services.SignupService.SignupResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.LOCATION;


@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class SignupController {

    private final SignupService signupService;

    @Operation(summary = "Signup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully signed up",
                    content = @Content(
                            mediaType = UserResource.CONTENT_TYPE,
                            schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = USERS, consumes = SignupRequest.CONTENT_TYPE, produces = UserResource.CONTENT_TYPE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest request,
            @Schema(example = "en-IN")
            @RequestHeader(name = ACCEPT_LANGUAGE, required = false) String language) {

        Locale locale = language == null ? Locale.ENGLISH : Locale.forLanguageTag(language);
        return toResponse(signupService.signup(request, locale));
    }

    private ResponseEntity<?> toResponse(SignupResult result) {
        return switch (result) {
            case SignupResult.Success success -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(LOCATION, USERS + "/" + success.response().id())
                    .body(success.response());
            case SignupResult.ValidationError error -> Problem.toResponse(error.problem());
        };
    }
}
