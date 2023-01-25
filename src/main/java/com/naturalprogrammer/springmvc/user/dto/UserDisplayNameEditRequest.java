package com.naturalprogrammer.springmvc.user.dto;

import com.naturalprogrammer.springmvc.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;

public record UserDisplayNameEditRequest(
        @NotBlank
        @Size(min = User.NAME_MIN, max = User.NAME_MAX)
        @Schema(example = "Sanjay Patel")
        String displayName
) {
    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "user-display-name-edit-request.v1+json";
}
