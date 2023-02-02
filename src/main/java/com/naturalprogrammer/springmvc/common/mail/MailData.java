package com.naturalprogrammer.springmvc.common.mail;

import org.springframework.core.io.InputStreamSource;

public record MailData(
        String to,
        String subject,
        String bodyHtml,
        Attachment attachment
) {
    public record Attachment(
            String name,
            InputStreamSource inputStreamSource,
            String contentType
    ) {
    }
}
