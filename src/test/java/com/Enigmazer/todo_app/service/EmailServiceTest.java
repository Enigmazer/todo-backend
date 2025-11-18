package com.Enigmazer.todo_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailService emailService;

    @Test
    void sendTaskReminder_shouldCallMailSender() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendTaskReminder("test@mail.com", "John", "Reminder", "Buy milk");

        verify(mailSender, times(1)).send(mimeMessage);
    }

}
