package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.toResponse;
import static com.naturalprogrammer.springmvc.common.Path.LOGIN;
import static com.naturalprogrammer.springmvc.common.Path.USERS;


@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
public class AuthTokenController {

    private final LoginService loginService;
    private final AuthTokenCreator authTokenCreator;
    private final ResourceTokenExchanger resourceTokenExchanger;
    private final AccessTokenCreator accessTokenCreator;

    @Operation(summary = "Login using email and password")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh and access tokens",
                    content = @Content(
                            mediaType = AuthTokensResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AuthTokensResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "401", description = "User not found or password doesn't match",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = LOGIN, consumes = LoginRequest.CONTENT_TYPE, produces = AuthTokensResource.CONTENT_TYPE)
    ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return toResponse(loginService.login(request), ResponseEntity::ok);
    }

    @Operation(summary = "Create tokens using resource token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh and access token",
                    content = @Content(
                            mediaType = AuthTokensResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AuthTokensResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @GetMapping(value = USERS + "/{id}/auth-tokens", produces = AuthTokensResource.CONTENT_TYPE)
    ResponseEntity<?> createAuthTokens(
            @PathVariable UUID id,
            @RequestParam(required = false) Long resourceTokenValidForMillis
    ) {
        return toResponse(authTokenCreator.create(id, resourceTokenValidForMillis), ResponseEntity::ok);
    }

    @Operation(summary = "Create tokens using client specific resource token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh and access token",
                    content = @Content(
                            mediaType = AuthTokensResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AuthTokensResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found or invalid client or insufficient rights",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(
            value = USERS + "/{id}/exchange-resource-token",
            consumes = ResourceTokenExchangeRequest.CONTENT_TYPE,
            produces = AuthTokensResource.CONTENT_TYPE
    )
    ResponseEntity<?> exchangeResourceToken(
            @PathVariable UUID id,
            @RequestBody ResourceTokenExchangeRequest exchangeRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return toResponse(resourceTokenExchanger.exchange(id, exchangeRequest, request, response), ResponseEntity::ok);
    }

    @Operation(summary = "Get Access Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token",
                    content = @Content(
                            mediaType = AccessTokenResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AccessTokenResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @GetMapping(value = USERS + "/{id}/access-token", produces = AccessTokenResource.CONTENT_TYPE)
    ResponseEntity<?> createAccessToken(
            @PathVariable UUID id
    ) {
        return toResponse(accessTokenCreator.create(id), ResponseEntity::ok);
    }
}
