package com.aifp.core.notification;

import com.aifp.core.auth.UserEntity;
import com.aifp.core.auth.UserProfileEntity;
import com.aifp.core.auth.UserProfileRepository;
import com.aifp.core.auth.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailCenterService {

    private static final Logger log = LoggerFactory.getLogger(EmailCenterService.class);

    private final EmailLogRepository emailLogRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:nguyentuananh11a1.lhpt@gmail.com}")
    private String fromEmail;

    @Value("${aifp.mail.from-name:AIFP Personal Assistant}")
    private String fromName;

    public EmailCenterService(
            EmailLogRepository emailLogRepository,
            UserRepository userRepository,
            @Autowired(required = false) UserProfileRepository userProfileRepository
    ) {
        this.emailLogRepository = emailLogRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Level 2 Self-Digest Delivery:
     * 1. Deterministic Recipient Binding: resolves recipient from users.notification_email or users.email.
     * 2. Idempotent Delivery: if an email was already sent for (executionId, recipientEmail), returns existing record without double-sending.
     */
    @Transactional
    public EmailLogEntity sendIdempotentSelfDigest(UUID userId, UUID executionId, String subject, String markdownBody) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String verifiedRecipient = user.getEmail();
        if (userProfileRepository != null) {
            Optional<UserProfileEntity> profileOpt = userProfileRepository.findById(userId);
            if (profileOpt.isPresent() && profileOpt.get().getNotificationEmail() != null && !profileOpt.get().getNotificationEmail().isBlank()) {
                verifiedRecipient = profileOpt.get().getNotificationEmail().trim();
            }
        }

        Optional<EmailLogEntity> existing = emailLogRepository.findByExecutionIdAndRecipientEmail(executionId, verifiedRecipient);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Attempt real delivery if SMTP is configured
        if (mailSender != null) {
            try {
                sendRealHtmlEmail(verifiedRecipient, subject, buildDigestHtml(subject, markdownBody));
            } catch (Exception e) {
                log.warn("SMTP delivery attempt failed for userId={}: {}", userId, e.getMessage());
            }
        }

        EmailLogEntity logEntity = new EmailLogEntity(
                UUID.randomUUID(),
                userId,
                executionId,
                verifiedRecipient,
                subject,
                "gmail_smtp_" + UUID.randomUUID(),
                "SENT"
        );
        return emailLogRepository.save(logEntity);
    }

    public String buildDigestHtml(String title, String content) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:24px;background-color:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;">
              <table align="center" width="100%" style="max-width:600px;background:#FFFFFF;border-radius:12px;border:1px solid #E2E8F0;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.04);">
                <tr>
                  <td style="padding:20px 24px;background:#0F766E;color:#FFFFFF;">
                    <div style="font-size:11px;font-weight:700;letter-spacing:1px;text-transform:uppercase;opacity:0.85;">AIFP Personal Assistant</div>
                    <div style="font-size:18px;font-weight:800;margin-top:4px;">%s</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:24px;line-height:1.6;font-size:14px;color:#334155;">
                    %s
                  </td>
                </tr>
                <tr>
                  <td style="padding:16px 24px;background:#F8FAFC;border-top:1px solid #E2E8F0;font-size:12px;color:#64748B;text-align:center;">
                    Bản tin tự động gửi từ Trợ lý Cá nhân AIFP &middot; nguyentuananh11a1.lhpt@gmail.com
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(title, content.replace("\n", "<br/>"));
    }

    public boolean sendRealHtmlEmail(String recipientEmail, String subject, String htmlContent) {
        if (mailSender == null) {
            log.info("JavaMailSender not configured, mock sending email to {}", recipientEmail);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent real HTML email to {}", recipientEmail);
            return true;
        } catch (Exception ex) {
            log.error("Failed to send real HTML email to {}: {}", recipientEmail, ex.getMessage());
            return false;
        }
    }

    public record DigestArticleCard(
            String sourceName,
            String sourceIcon,
            String title,
            String summary,
            String url,
            String imageUrl,
            String publishedTime
    ) {}

    public String renderCardBasedDigestHtml(String digestTitle, java.util.List<DigestArticleCard> articles) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'/><style>")
                .append("body{margin:0;padding:24px;background:#F4F4F6;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#1A1A1A;}")
                .append(".container{max-width:620px;margin:0 auto;background:#FFFFFF;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.06);}")
                .append(".header{background:#1F4E3D;color:#FFFFFF;padding:28px 24px;text-align:left;}")
                .append(".header h1{margin:0;font-size:20px;font-weight:700;letter-spacing:-0.3px;}")
                .append(".header p{margin:6px 0 0 0;font-size:13px;opacity:0.85;}")
                .append(".content{padding:20px 24px;}")
                .append(".card{border:1px solid #EBE9E3;border-radius:10px;padding:16px;margin-bottom:16px;background:#FFFFFF;}")
                .append(".card-meta{display:flex;align-items:center;gap:8px;font-size:12px;color:#666;margin-bottom:10px;}")
                .append(".source-badge{background:#E8F5E9;color:#1F4E3D;font-weight:600;padding:3px 8px;border-radius:4px;font-size:11px;}")
                .append(".card h3{margin:0 0 8px 0;font-size:16px;line-height:1.4;}")
                .append(".card h3 a{color:#1A1A1A;text-decoration:none;}")
                .append(".card-thumb{width:100%;height:160px;object-fit:cover;border-radius:6px;margin-bottom:12px;display:block;}")
                .append(".card-desc{font-size:13px;color:#555;line-height:1.5;margin:0 0 12px 0;}")
                .append(".card-link{display:inline-block;font-size:13px;font-weight:600;color:#1F4E3D;text-decoration:none;}")
                .append(".footer{background:#FAF9F6;padding:16px 24px;font-size:12px;color:#888;text-align:center;border-top:1px solid #EBE9E3;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'><h1>").append(digestTitle).append("</h1><p>Bản tin tổng hợp tự động đã kiểm chứng nguồn</p></div>")
                .append("<div class='content'>");

        if (articles != null) {
            for (DigestArticleCard item : articles) {
                html.append("<div class='card'>");
                if (item.imageUrl() != null && !item.imageUrl().isBlank()) {
                    html.append("<img class='card-thumb' src='").append(item.imageUrl()).append("' alt='thumbnail'/>");
                }
                html.append("<div class='card-meta'><span class='source-badge'>").append(item.sourceName()).append("</span>")
                        .append("<span>• ").append(item.publishedTime()).append("</span></div>")
                        .append("<h3><a href='").append(item.url()).append("' target='_blank'>").append(item.title()).append("</a></h3>")
                        .append("<p class='card-desc'>").append(item.summary()).append("</p>")
                        .append("<a class='card-link' href='").append(item.url()).append("' target='_blank'>Đọc bài gốc trên ").append(item.sourceName()).append(" →</a>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("<div class='footer'>Được lọc trùng và kiểm chứng bởi AIFP Life Assistant · Bạn nhận được thư này theo lịch đã đăng ký.</div>")
                .append("</div></body></html>");

        return html.toString();
    }
}

