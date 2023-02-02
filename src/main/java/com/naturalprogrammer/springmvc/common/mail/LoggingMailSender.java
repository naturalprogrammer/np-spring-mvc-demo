package com.naturalprogrammer.springmvc.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile({"default", "test"})
public class LoggingMailSender implements MailSender {

    private static final List<MailData> SENT_MAILS = new ArrayList<>();

    @Override
    public void send(MailData mail) {
        SENT_MAILS.add(mail);
        log.info("Sending {}", mail);
    }

    public static List<MailData> sentMails() {
        return SENT_MAILS;
    }
}
