package com.naturalprogrammer.springmvc.user.features.change_password;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.naturalprogrammer.springmvc.common.Path.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping(USER)
@Tag(name = "User", description = "User API")
public class ChangePasswordController {

    private final PasswordChanger passwordChanger;

    @Operation(summary = "Change password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "403", description = "Old password mismatch",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PatchMapping(value = "/password", consumes = ChangePasswordRequest.CONTENT_TYPE)
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        return passwordChanger
                .changePassword(request)
                .map(Problem::toResponse)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
