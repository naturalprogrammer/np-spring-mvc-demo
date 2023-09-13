package com.naturalprogrammer.springmvc.user.features.get;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.CommonUtils.toResponse;
import static com.naturalprogrammer.springmvc.common.Path.USERS;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
class GetUserController {

    private final UserGetter userGetter;

    @Operation(summary = "Get User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User",
                    content = @Content(
                            mediaType = UserResource.CONTENT_TYPE,
                            schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found or insufficient rights (must be self or admin)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @GetMapping(value = "/{id}", produces = UserResource.CONTENT_TYPE)
    ResponseEntity<?> getUser(@PathVariable UUID id) {
        return toResponse(userGetter.get(id), ResponseEntity::ok);
    }
}
