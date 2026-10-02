package com.sochtech.loginandsignup.configuration;

import com.sochtech.loginandsignup.dtos.MailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    private final JavaMailSender javaMailSender;

    public EmailUtils(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(MailBody mailBody) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true,  "UTF-8");

        helper.setTo(mailBody.to());
        helper.setFrom("YOUR EMAIL");
        helper.setSubject(mailBody.subject());
        helper.setText(mailBody.text(), true); // set to true for HTML content

        javaMailSender.send(message);
    }
}
