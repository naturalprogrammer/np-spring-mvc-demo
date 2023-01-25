package com.naturalprogrammer.springmvc.user.controllers;

import com.naturalprogrammer.springmvc.common.error.Problem;
import com.naturalprogrammer.springmvc.user.dto.UserDisplayNameEditRequest;
import com.naturalprogrammer.springmvc.user.dto.UserResource;
import com.naturalprogrammer.springmvc.user.services.DisplayNameEditor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.naturalprogrammer.springmvc.common.Path.USERS;
import static org.springframework.http.HttpHeaders.LOCATION;

@RestController
@RequiredArgsConstructor
@Tag(name = "User")
public class EditDisplayNameController {

    private final DisplayNameEditor displayNameEditor;

    @Operation(summary = "Edit display name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Display name updated",
                    content = @Content(
                            mediaType = UserResource.CONTENT_TYPE,
                            schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "422", description = "Invalid input",
                    content = @Content(
                            mediaType = Problem.CONTENT_TYPE,
                            schema = @Schema(implementation = Problem.class))
            )
    })
    @PatchMapping(value = USERS + "/{id}/display-name",
            consumes = UserDisplayNameEditRequest.CONTENT_TYPE,
            produces = UserResource.CONTENT_TYPE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> editDisplayName(
            @PathVariable UUID id,
            @RequestBody UserDisplayNameEditRequest request
    ) {
        return toResponse(displayNameEditor.edit(id, request));
    }

    private ResponseEntity<?> toResponse(DisplayNameEditor.Result result) {
        return switch (result) {
            case DisplayNameEditor.Result.Success success -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header(LOCATION, USERS + "/" + success.response().id())
                    .body(success.response());
            case DisplayNameEditor.Result.Error error -> Problem.toResponse(error.problem());
        };
    }

}
