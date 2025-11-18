package com.Enigmazer.todo_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending email notifications.
 * Handles the construction and sending of HTML-formatted emails.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends an HTML-formatted email notification for task reminders.
     * The email includes the task title and due date in a styled format.
     *
     * @param to The recipient's email address
     * @param name The recipient's full name
     * @param subject The email subject line
     * @param taskTitle The title of the task being reminded about
     * @throws MessagingException if there is an error sending the email
     */
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
//
//    /**
//     * Sends an email notification for support.
//     * The email includes the user's name, support category, subject and message.
//     *
//     * @param name The user's name
//     * @param subject The support subject
//     * @param message The support message
//     * @throws MessagingException if there is an error sending the email
//     */
//    public void sendSupportEmail(String name, String email, String subject, String category,
//                                 String message) throws MessagingException {
//
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false); // plain text
//
//        helper.setTo("arundangi660@gmail.com");
//        helper.setSubject(subject);
//        helper.setText(
//                "Name: " + name + "<br>" +
//                        "Email: " + email + "<br>" +
//                        "Category: " + category + "<br>" +
//                        "Message: " + message, true
//        );
//
//        mailSender.send(mimeMessage);
//    }

}
