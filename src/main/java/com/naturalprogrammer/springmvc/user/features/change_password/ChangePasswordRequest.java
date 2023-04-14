package com.naturalprogrammer.springmvc.user.features.change_password;

import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;
import static com.naturalprogrammer.springmvc.user.validators.PasswordValidator.PASSWORD_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.trim;

public record ChangePasswordRequest(

        @ValidPassword
        @Schema(example = "YourOldPassword9!", description = PASSWORD_DESCRIPTION)
        String oldPassword,

        @ValidPassword
        @Schema(example = "YourNewPassword9!", description = PASSWORD_DESCRIPTION)
        String newPassword

) {

    public ChangePasswordRequest trimmed() {
        return new ChangePasswordRequest(trim(oldPassword), trim(newPassword));
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{}";
    }

    public static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "change-password-request.v1+json";
}
