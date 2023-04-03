package com.naturalprogrammer.springmvc.user.features.verification;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.services.UserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.toResponse;
import static com.naturalprogrammer.springmvc.common.Path.USERS;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
class UserVerificationController {

    private final UserVerifier userVerifier;

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
            ),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights (must be self or admin)",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "403", description = "Token verification failed",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            ),
    })
    @PutMapping(value = "/{id}/verifications",
            consumes = UserVerificationRequest.CONTENT_TYPE,
            produces = UserResource.CONTENT_TYPE)
    public ResponseEntity<?> verifyEmail(
            @PathVariable UUID id,
            @RequestBody UserVerificationRequest request
    ) {
        return toResponse(userVerifier.verify(id, request), ResponseEntity::ok);
    }
}
