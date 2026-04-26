package com.sa.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${auth.verify-base-url:http://localhost:8080/api/v1/auth}")
    private String verifyBaseUrl;

    @Value("${app.mail.from:canhhuu205@gmail.com}")
    private String mailFrom;

    public void sendVerificationEmail(String to, String token) {
        String subject = "[Event Manager] Email Verification";
        String verificationUrl = verifyBaseUrl + "/verify?token=" + token;
        String message = "Please click the link below to verify your email:\n" + verificationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailFrom);
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);

        try {
            mailSender.send(email);
            System.out.println("✅ Đã gửi email xác thực thành công tới: " + to);
        } catch (Exception e) {
            System.err.println("⚠️ KHÔNG THỂ GỬI EMAIL XÁC THỰC! (Do chưa cấu hình SMTP).");
            System.err.println("👉 LINK XÁC THỰC CỦA BẠN LÀ: " + verificationUrl);
        }
    }
}

