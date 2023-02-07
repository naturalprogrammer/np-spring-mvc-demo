package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.dto.SignupRequest;
import com.naturalprogrammer.springmvc.user.dto.UserDisplayNameEditRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.dto.UserVerificationRequest;
import com.naturalprogrammer.springmvc.user.services.DisplayNameEditor;
import com.naturalprogrammer.springmvc.user.services.SignupService;
import com.naturalprogrammer.springmvc.user.usecases.verification.UserVerifier;
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
import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.toResponse;
import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.LOCATION;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
public class UserController {

    private final SignupService signupService;
    private final UserVerifier userVerifier;
    private final DisplayNameEditor displayNameEditor;

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
    @PostMapping(consumes = SignupRequest.CONTENT_TYPE, produces = UserResource.CONTENT_TYPE)
    public ResponseEntity<?> signup(
            @RequestBody SignupRequest request,
            @Schema(example = "en-IN")
            @RequestHeader(name = ACCEPT_LANGUAGE, required = false) String language) {

        Locale locale = language == null ? Locale.forLanguageTag("en-IN") : Locale.forLanguageTag(language);
        return toResponse(
                signupService.signup(request, locale),
                userResource ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .header(LOCATION, USERS + "/" + userResource.id())
                                .body(userResource)
        );
    }

    @Operation(summary = "Verify email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified",
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
    @PostMapping(value = "/{id}/verification",
            consumes = UserVerificationRequest.CONTENT_TYPE,
            produces = UserResource.CONTENT_TYPE)
    public ResponseEntity<?> verifyEmail(
            @PathVariable UUID id,
            @RequestBody UserVerificationRequest request
    ) {
        return toResponse(userVerifier.verify(id, request), ResponseEntity::ok);
    }

    @Operation(summary = "Edit display name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Display name updated",
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
    @PatchMapping(value = "/{id}/display-name",
            consumes = UserDisplayNameEditRequest.CONTENT_TYPE,
            produces = UserResource.CONTENT_TYPE)
    public ResponseEntity<?> editDisplayName(
            @PathVariable UUID id,
            @RequestBody UserDisplayNameEditRequest request
    ) {
        return toResponse(displayNameEditor.edit(id, request), ResponseEntity::ok);
    }
}
