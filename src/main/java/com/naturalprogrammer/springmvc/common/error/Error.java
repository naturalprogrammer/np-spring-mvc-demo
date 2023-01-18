package com.naturalprogrammer.springmvc.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record Error(

        @Schema(example = "not_blank")
        String code,

        @Schema(example = "Should not be blank")
        String message,

        @Schema(example = "email")
        String field
) {
}
