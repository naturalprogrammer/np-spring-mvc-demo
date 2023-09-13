package com.naturalprogrammer.springmvc.user.features.get_by_email;

import com.naturalprogrammer.springmvc.user.services.UserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.naturalprogrammer.springmvc.common.Path.USERS;


@RestController
@RequiredArgsConstructor
@RequestMapping(USERS)
@Tag(name = "User", description = "User API")
class GetUserByEmailController {

    private final UsersGetter usersGetter;

    @Operation(summary = "Get users by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users",
                    content = @Content(
                            mediaType = UserResource.LIST_TYPE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = UserResource.class)
                            )
                    )
            )
    })
    @GetMapping(produces = UserResource.LIST_TYPE)
    List<UserResource> getUsers(@RequestParam String email) {
        return usersGetter.getBy(email);
    }
}
