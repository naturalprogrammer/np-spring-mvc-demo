package com.naturalprogrammer.springmvc.user.features.signup;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static com.naturalprogrammer.springmvc.common.CommonUtils.toResponse;
import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.LOCATION;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
class SignupController {

    private final SignupService signupService;

    @Operation(summary = "Signup")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully signed up",
                    headers = {
                            @Header(
                                    name = LOCATION,
                                    description = "GET the user at this location",
                                    schema = @Schema(example = "/users/8fd1502e-759d-419f-aaac-e61478fc6406"))
                    },
                    content = @Content(
                            mediaType = UserResource.CONTENT_TYPE,
                            schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "409", description = "Email already used",
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
}
