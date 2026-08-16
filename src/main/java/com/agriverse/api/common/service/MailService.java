package com.agriverse.api.common.service;

import com.agriverse.api.common.config.FrontendProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Thin mail-sending seam used by the Auth flows (verification, password
 * reset). Sends real HTML emails with a clickable button linking straight
 * into the frontend's /verify-email and /reset-password pages (token in
 * the query string) -- not a raw token the person has to copy into a form
 * by hand. Frontend base URL is configurable
 * ({@code agriverse.frontend.base-url} / {@code FRONTEND_BASE_URL}) so
 * this points at the right place in every environment.
 *
 * Fire-and-forget async send with a log fallback, so a misconfigured SMTP
 * provider in dev never blocks registration/login flows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final FrontendProperties frontendProperties;

    @Async
    public void sendVerificationEmail(String to, String fullName, String rawToken) {
        String link = buildLink("/verify-email", rawToken);
        String html = wrapTemplate(
                "Welcome to AgriVerse, " + escape(fullName) + "!",
                "Verify your email address to finish setting up your account.",
                link, "Verify email address",
                "This link expires in 48 hours. If you didn't create an AgriVerse account, you can safely ignore this email.");
        send(to, "Verify your AgriVerse account", html);
    }

    @Async
    public void sendPasswordResetEmail(String to, String fullName, String rawToken) {
        String link = buildLink("/reset-password", rawToken);
        String html = wrapTemplate(
                "Reset your password",
                "Hi " + escape(fullName) + ", we received a request to reset your AgriVerse password.",
                link, "Reset password",
                "This link expires in 30 minutes. If you didn't request this, you can safely ignore this email -- your password won't be changed.");
        send(to, "Reset your AgriVerse password", html);
    }

    private String buildLink(String path, String rawToken) {
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        return frontendProperties.getBaseUrl() + path + "?token=" + encodedToken;
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send email to {} ('{}'); continuing without blocking the caller: {}", to, subject, e.getMessage());
        }
    }

    /**
     * Minimal inline-styled HTML -- deliberately no external stylesheet or
     * images, since most mail clients strip or block both, and inline
     * styles are the one thing that reliably renders everywhere (Gmail,
     * Outlook, Apple Mail alike).
     */
    private String wrapTemplate(String heading, String message, String link, String buttonText, String footnote) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0;padding:0;background-color:#f4f6f4;font-family:Georgia,'Times New Roman',serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="padding:32px 16px;">
                    <tr><td align="center">
                      <table role="presentation" width="100%%" style="max-width:480px;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e2e8e2;">
                        <tr><td style="background-color:#1f3d2b;padding:24px 32px;">
                          <span style="color:#f4f6f4;font-size:20px;font-weight:600;">AgriVerse</span>
                        </td></tr>
                        <tr><td style="padding:32px;">
                          <h1 style="margin:0 0 16px;font-size:22px;color:#1f3d2b;">%s</h1>
                          <p style="margin:0 0 24px;font-size:15px;line-height:1.5;color:#333333;">%s</p>
                          <table role="presentation" cellpadding="0" cellspacing="0">
                            <tr><td style="border-radius:8px;background-color:#2f6b3e;">
                              <a href="%s" style="display:inline-block;padding:12px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;">%s</a>
                            </td></tr>
                          </table>
                          <p style="margin:24px 0 0;font-size:13px;line-height:1.5;color:#767676;">%s</p>
                          <p style="margin:16px 0 0;font-size:12px;line-height:1.5;color:#a0a0a0;">
                            Button not working? Copy and paste this link into your browser:<br>
                            <span style="word-break:break-all;">%s</span>
                          </p>
                        </td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(heading, message, link, buttonText, footnote, link);
    }

    private String escape(String input) {
        return input == null ? "" : input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
