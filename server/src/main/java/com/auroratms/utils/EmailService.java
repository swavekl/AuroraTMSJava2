package com.auroratms.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

//    @Value("${spring.mail.username}")
    private String fromAddress = "swaveklorenc@gmail.com";

    public void sendEmail (String toAddress, String subject, String emailBody) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(emailBody);
        this.javaMailSender.send(simpleMailMessage);
    }
}
