package com.github.humbletrader.fmak.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendEmail(String to, String content){
        log.info("sending email to {}", to);
    }

}
