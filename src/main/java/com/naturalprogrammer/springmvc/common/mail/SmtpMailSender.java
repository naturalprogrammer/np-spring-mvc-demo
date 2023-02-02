package com.naturalprogrammer.springmvc.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"staging", "live"})
public class SmtpMailSender implements MailSender {

    private final JavaMailSender javaMailSender;

    @Async
    @SneakyThrows
    @Override
    public void send(MailData mail) {

        log.info("Sending {}", mail);
        var message = javaMailSender.createMimeMessage();

        // true = multipart message
        var helper = new MimeMessageHelper(message, true);
        helper.setTo(mail.to());
        helper.setSubject(mail.subject());
        helper.setText(mail.bodyHtml(), true);

        var attachment = mail.attachment();
        if (attachment != null) {
            helper.addAttachment(attachment.name(), attachment.inputStreamSource(), attachment.contentType());
        }

        javaMailSender.send(message);
    }
}
