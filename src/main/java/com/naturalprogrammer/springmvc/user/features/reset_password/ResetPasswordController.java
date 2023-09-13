package com.naturalprogrammer.springmvc.user.features.reset_password;

import com.naturalprogrammer.springmvc.common.error.Problem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.naturalprogrammer.springmvc.common.Path.RESET_PASSWORD;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
class ResetPasswordController {

    private final PasswordResetter passwordResetter;

    @Operation(summary = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Forgot password link mailed"),
            @ApiResponse(responseCode = "422", description = "Token absent or too long",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "403", description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found. Got deleted?",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )})
    @PostMapping(value = RESET_PASSWORD, consumes = ResetPasswordRequest.CONTENT_TYPE)
    ResponseEntity<?> forgotPassword(@RequestBody ResetPasswordRequest request) {

        return passwordResetter
                .reset(request)
                .map(Problem::toResponse)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
