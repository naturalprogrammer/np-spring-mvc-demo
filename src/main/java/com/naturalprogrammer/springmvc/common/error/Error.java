package com.naturalprogrammer.springmvc.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record Error(

        @Schema(example = "NotBlank")
        String code,

        @Schema(example = "must not be blank")
        String message,

        @Schema(example = "email")
        String field
) {
}
