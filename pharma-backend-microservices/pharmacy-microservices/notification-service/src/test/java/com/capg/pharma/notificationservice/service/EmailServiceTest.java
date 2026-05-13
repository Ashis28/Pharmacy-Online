package com.capg.pharma.notificationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for EmailService — covers enabled/disabled flag and SMTP failure handling.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;

    EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromAddress", "test@example.com");
    }

    @Test
    void sendEmail_whenDisabled_doesNotCallMailSender() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        emailService.sendEmail("to@example.com", "Subject", "Body");

        verifyNoInteractions(mailSender);
    }

    @Test
    void sendEmail_whenEnabled_sendsEmail() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("to@example.com", "Test Subject", "Test Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_whenEnabled_andMailFails_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        doThrow(new MailSendException("SMTP connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Must not propagate — email failure should never break the notification flow
        assertThatNoException().isThrownBy(
                () -> emailService.sendEmail("to@example.com", "Subject", "Body")
        );
    }

    @Test
    void sendEmail_whenDisabled_withNullFields_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        assertThatNoException().isThrownBy(
                () -> emailService.sendEmail(null, null, null)
        );
        verifyNoInteractions(mailSender);
    }
}
