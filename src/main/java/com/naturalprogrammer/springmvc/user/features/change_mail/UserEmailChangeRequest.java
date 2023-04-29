package com.naturalprogrammer.springmvc.user.features.change_mail;

import com.naturalprogrammer.springmvc.user.domain.User;
import com.naturalprogrammer.springmvc.user.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import static com.naturalprogrammer.springmvc.common.CommonUtils.CONTENT_TYPE_PREFIX;
import static com.naturalprogrammer.springmvc.user.validators.PasswordValidator.PASSWORD_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
record UserEmailChangeRequest(

        @Email
        @NotBlank
        @Size(max = User.EMAIL_MAX)
        @Schema(example = "sanjay@example.com")
        String oldEmail,

        @ValidPassword
        @Schema(example = "Secret99!", description = PASSWORD_DESCRIPTION)
        String password,

        @Email
        @NotBlank
        @Size(max = User.EMAIL_MAX)
        @Schema(example = "sanjay@example.com")
        String newEmail
) {

    public UserEmailChangeRequest trimmed() {
        var trimmed = new UserEmailChangeRequest(trim(oldEmail), trim(password), trim(newEmail));
        log.info("Trimmed {} to  {}", this, trimmed);
        return trimmed;
    }

    @Override
    public String toString() {
        return "UserEmailChangeRequest{" +
                "oldEmail='" + oldEmail + '\'' +
                ", newEmail='" + newEmail + '\'' +
                '}';
    }

    static final String CONTENT_TYPE = CONTENT_TYPE_PREFIX + "user-email-change-request.v1+json";
}
