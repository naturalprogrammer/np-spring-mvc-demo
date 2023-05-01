package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.naturalprogrammer.springmvc.common.Path.USER;


@RestController
@RequiredArgsConstructor
@RequestMapping(USER)
@Tag(name = "User", description = "User API")
class ChangeEmailController {

    private final EmailChangeRequestProcessor emailChangeRequestProcessor;
    private final EmailChanger emailChanger;

    @Operation(summary = "Request changing email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email change requested"),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "403", description = "Old email or password mismatch",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights (must be self)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = "/email-change-request", consumes = UserEmailChangeRequest.CONTENT_TYPE)
    public ResponseEntity<?> requestChangingEmail(@RequestBody UserEmailChangeRequest request) {
        return emailChangeRequestProcessor
                .process(request)
                .map(Problem::toResponse)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Verify new email and update")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Email changed. Old auth tokens invalidated"),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "403", description = "Token verification failed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "409", description = "New email already used",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PatchMapping(value = "/email-change-request",
            consumes = UserEmailChangeVerificationRequest.CONTENT_TYPE,
            produces = UserResource.CONTENT_TYPE)
    public ResponseEntity<?> changeEmail(@RequestBody UserEmailChangeVerificationRequest request) {
        return emailChanger.changeEmail(request)
                .map(Problem::toResponse)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
