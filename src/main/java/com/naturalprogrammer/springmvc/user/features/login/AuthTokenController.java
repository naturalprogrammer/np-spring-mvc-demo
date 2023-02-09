package com.naturalprogrammer.springmvc.user.features.login;

import com.naturalprogrammer.springmvc.common.error.Problem;
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
@Tag(name = "User", description = "User API")
public class AuthTokenController {

    private final AuthTokenCreator authTokenCreator;
    private final AccessTokenCreator accessTokenCreator;

    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created Auth Tokens",
                    content = @Content(
                            mediaType = AuthTokenResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AuthTokenResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PostMapping(value = "/auth-token", consumes = AuthTokenRequest.CONTENT_TYPE, produces = AuthTokenRequest.CONTENT_TYPE)
    public ResponseEntity<?> createAuthToken(
            @RequestBody AuthTokenRequest request
    ) {
        return toResponse(authTokenCreator.login(request), ResponseEntity::ok);
    }

    @Operation(summary = "Get Access Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access Token",
                    content = @Content(
                            mediaType = AccessTokenResource.CONTENT_TYPE,
                            schema = @Schema(implementation = AccessTokenResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @GetMapping(value = USERS + "/{id}/access-token", produces = AccessTokenResource.CONTENT_TYPE)
    public ResponseEntity<?> createAccessToken(
            @PathVariable UUID id
    ) {
        return toResponse(accessTokenCreator.create(id), ResponseEntity::ok);
    }
}
