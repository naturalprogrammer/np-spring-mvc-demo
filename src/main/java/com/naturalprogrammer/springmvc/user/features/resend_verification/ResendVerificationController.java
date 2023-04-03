package com.naturalprogrammer.springmvc.user.features.resend_verification;

import com.naturalprogrammer.springmvc.common.error.Problem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
class ResendVerificationController {

    @Operation(summary = "Resend verification mail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Verification mail resent"),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights (must be self or admin)",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "409", description = "User already verified",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = "/{id}/verifications")
    public ResponseEntity<?> resendVerification(@PathVariable UUID id) {

        //TODO: fill it
        return null;
    }
}
