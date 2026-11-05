package com.veth007.hotelmanagement.service.EmailVerifire.Implement;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.veth007.hotelmanagement.config.Validation.GmailDomainValidator;
import com.veth007.hotelmanagement.exception.AlreadyExistException;
import com.veth007.hotelmanagement.exception.ApiResponse;
import com.veth007.hotelmanagement.exception.BusinessException;
import com.veth007.hotelmanagement.exception.NotFoundException;
import com.veth007.hotelmanagement.model.entity.EmailVerificationToken;
import com.veth007.hotelmanagement.model.entity.EmailVerificationToken.TokenStatus;
import com.veth007.hotelmanagement.repository.CustomerRepository;
import com.veth007.hotelmanagement.repository.EmailVerificationTokenRepository;
import com.veth007.hotelmanagement.service.EmailVerifire.EmailVerificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImp implements EmailVerificationService{

    private final EmailVerificationTokenRepository tokenRepository;
    private final CustomerRepository customerRepository;
    private final JavaMailSender mailSender;
    private final GmailDomainValidator gmailDomainValidator;
    private static final int MAX_ATTEMPTS = 5;

    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.token.expiry-minutes}")
    private int expiryMinutes;
    @Value("${app.mail.from}")
    private String fromEmail;
    @Value("${app.mail.dev-mode:false}")
    private boolean devMode;

    @Override
    @Transactional
    public ApiResponse<String> sendVerification(String email) {
        String normalized = email.trim().toLowerCase();

        if (!gmailDomainValidator.isGoogleEmail(normalized))
            throw new IllegalArgumentException("Not a Gmail address.");

        tokenRepository.findByEmail(normalized).ifPresent(t -> {
            if (t.getStatus() == TokenStatus.VERIFIED)
                throw new AlreadyExistException("This Gmail address is already verified.");
            tokenRepository.delete(t);
            tokenRepository.flush();
        });

        LocalDateTime now = LocalDateTime.now();

        String opt = generateOtp();
        tokenRepository.save(EmailVerificationToken.builder()
                .email(normalized)
                .token(opt)
                .status(TokenStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusMinutes(expiryMinutes))
                .build());

        sendOtpEmail(normalized, opt);
        log.info("Verification email sent to {}", normalized);
        return ApiResponse.ok("Verification email sent. Check your Gmail inbox.", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyOtp(String email, String otpCode) {
        EmailVerificationToken record = tokenRepository.findByEmailAndToken(email, otpCode)
                .orElseThrow(() -> new NotFoundException("Invalid verification link."));

        if (record.getStatus() == TokenStatus.VERIFIED)
            throw new AlreadyExistException("Email already verified.");

        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            record.setStatus(TokenStatus.EXPIRED);
            tokenRepository.save(record);
            throw new BusinessException("Link expired. Please request a new verification email.");
        }

        if (!record.getToken().equals(otpCode)) {
            record.setAttemptCount(record.getAttemptCount() + 1);
            tokenRepository.save(record);
            throw new BusinessException("Invalid OTP code.");
        }
        if (record.getAttemptCount() >= MAX_ATTEMPTS) {
            tokenRepository.delete(record);
            throw new BusinessException("Too many failed attempts. Please request a new OTP.");
        }

        record.setStatus(TokenStatus.VERIFIED);
        record.setVerifiedAt(LocalDateTime.now());
        tokenRepository.save(record);

        customerRepository.findByEmail(record.getEmail()).ifPresent(customer -> {
            customer.setEmailVerified(true);
            customerRepository.save(customer);
        });

        log.info("Email verified: {}", record.getEmail());
        return ApiResponse.ok("Email verified successfully!", record.getEmail());
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit
        return String.valueOf(code);
    }

    @Override
    public ApiResponse<String> checkStatus(String email) {
        String normalized = email.trim().toLowerCase();

        if (!gmailDomainValidator.isGoogleEmail(normalized))
            throw new IllegalArgumentException("Not a Gmail address.");

        EmailVerificationToken t = tokenRepository.findByEmail(normalized)
                .orElseThrow(() -> new NotFoundException("No verification record found."));

        return switch (t.getStatus()) {
            case VERIFIED -> ApiResponse.ok("Email is verified.", t.getEmail());
            case PENDING -> ApiResponse.ok("Verification pending — check your Gmail inbox.", null);
            case EXPIRED -> throw new BusinessException("Token expired. Please request a new verification email.");
        };
    }

    private void sendOtpEmail(String to, String otpCode) {
        String body = buildOtpEmailHtml(otpCode);

        if (devMode) {
            log.info("=== DEV MODE === To: {} | OTP: {}", to, otpCode);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Your Verification Code");
            helper.setText(body, true); // true = HTML
            mimeMessage.setHeader("Return-Path", fromEmail);
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            tokenRepository.deleteByEmail(to);
            log.error("OTP mail send failed for {}: {}", to, e.getMessage(), e);

            String errMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (errMsg.contains("authentication failed") || errMsg.contains("535"))
                throw new RuntimeException("Gmail auth failed. Use an App Password if 2FA is enabled.");
            if (errMsg.contains("550") || errMsg.contains("user unknown") || errMsg.contains("does not exist"))
                throw new IllegalArgumentException("This Gmail address does not exist.");

            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    private String buildOtpEmailHtml(String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background-color:#f4f6f8;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="480" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 10px rgba(0,0,0,0.06);">
                          <tr>
                            <td style="background:#3478c8;padding:28px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:20px;">Hotel Management</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:32px 32px 8px;text-align:center;">
                              <p style="margin:0 0 8px;color:#333;font-size:16px;">Your verification code is</p>
                              <div style="margin:16px 0;font-size:36px;font-weight:700;letter-spacing:8px;color:#3478c8;">
                                %s
                              </div>
                              <p style="margin:0;color:#888;font-size:13px;">This code expires in %d minutes</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:24px 32px 32px;text-align:center;">
                              <p style="margin:0;color:#999;font-size:12px;line-height:1.5;">
                                If you did not request this code, you can safely ignore this email.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#f4f6f8;padding:16px;text-align:center;">
                              <p style="margin:0;color:#aaa;font-size:11px;">© Hotel Management Team</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """
                .formatted(otpCode, expiryMinutes);
    }
}
