package com.naturalprogrammer.springmvc.user.features.forgot_password;

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

import static com.naturalprogrammer.springmvc.common.Path.FORGOT_PASSWORD;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
class ForgotPasswordController {

    private final ForgotPasswordInitiator forgotPasswordInitiator;

    @Operation(summary = "Forgot password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Forgot password link mailed"),
            @ApiResponse(responseCode = "422", description = "Invalid email",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = FORGOT_PASSWORD, consumes = ForgotPasswordRequest.CONTENT_TYPE)
    ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {

        return forgotPasswordInitiator
                .initiate(request)
                .map(Problem::toResponse)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
