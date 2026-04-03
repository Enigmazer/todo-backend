package com.Enigmazer.todo_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Transactional
    public void sendTaskReminder(String to, String name, String subject, String taskTitle)
            throws MessagingException{

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);

        String htmlContent = """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px; border-radius: 10px; background: #f9f9f9;">
                      <h2 style="color: #2c3e50;">⏰ Task Reminder</h2>
                      <p>Hey %s,</p>
                      <div style="padding: 15px; margin: 10px 0; background: #ecf0f1; border-radius: 8px;">
                      <p style="font-size: 14px;">This is a reminder that your task <strong>"%s"</strong> is due in less than 1 hour!:</p>
                      </div>
                      <p>Make sure you wrap this up before the deadline 🚀</p>
                      <hr style="border:none; border-top:1px solid #ddd;"/>
                      <p style="font-size: 12px; color: #888;">This is an automated reminder from <strong>todo--master</strong>. Do not reply.</p>
                    </div>
                  </body>
                </html>
                """.formatted(name, taskTitle);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
