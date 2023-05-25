package com.naturalprogrammer.springmvc.user.features.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is a dummy controller, just for OpenAPI documentation
 * The actual work happens in a filter provided by Spring
 */
@RestController
@Tag(name = "User", description = "User API")
public class SocialLoginController {

    @Operation(summary = "Google Login")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to Google login")
    })
    @GetMapping("/oauth2/authorization/google")
    public void googleLogin() {
    }

}
