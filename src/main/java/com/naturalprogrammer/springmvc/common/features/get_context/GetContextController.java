package com.naturalprogrammer.springmvc.common.features.get_context;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Core", description = "Core API")
public class GetContextController {

    private final ContextGetter contextGetter;

    @Operation(summary = "Get context")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Context",
                    content = @Content(
                            mediaType = ContextResource.CONTENT_TYPE,
                            schema = @Schema(implementation = ContextResource.class))
            )
    })
    @GetMapping(value = "/context", produces = ContextResource.CONTENT_TYPE)
    public ContextResource getContext() {
        return contextGetter.get();
    }

}
