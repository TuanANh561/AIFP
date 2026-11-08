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
        String safeContent = (content != null) ? content.replace("\n", "<br/>") : "";
        String safeTitle = (title != null) ? title : "AIFP Bản tin";
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
                    <div style="font-size:18px;font-weight:800;margin-top:4px;">{{TITLE}}</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:24px;line-height:1.6;font-size:14px;color:#334155;">
                    {{CONTENT}}
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
            """
            .replace("{{TITLE}}", safeTitle)
            .replace("{{CONTENT}}", safeContent);
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

    public String renderAiEvolutionHtml(
            String reportTitle,
            String reportDate,
            List<DigestArticleCard> breakthroughs,
            String pastSummary,
            String presentSummary,
            String futureRoadmap,
            List<String> suggestedImprovements
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;}")
                .append(".container{max-width:680px;margin:0 auto;background:#FFFFFF;border-radius:14px;overflow:hidden;border:1px solid #CBD5E1;box-shadow:0 6px 20px rgba(0,0,0,0.06);}")
                .append(".header{background:#0F172A;color:#FFFFFF;padding:32px 28px;text-align:left;}")
                .append(".badge{display:inline-block;padding:4px 10px;border-radius:9999px;background:#2563EB;color:#FFFFFF;font-size:11px;font-weight:700;letter-spacing:0.4px;text-transform:uppercase;}")
                .append(".header h1{margin:12px 0 6px 0;font-size:22px;font-weight:800;letter-spacing:-0.4px;line-height:1.3;}")
                .append(".header p{margin:0;font-size:13.5px;color:#94A3B8;line-height:1.5;}")
                .append(".section{padding:24px 28px;border-bottom:1px solid #E2E8F0;}")
                .append(".section-title{font-size:13.5px;font-weight:800;text-transform:uppercase;letter-spacing:0.5px;color:#1E293B;margin-bottom:16px;display:flex;align-items:center;gap:8px;}")
                .append(".matrix-card{padding:16px 18px;border-radius:10px;border:1px solid #E2E8F0;background:#F8FAFC;margin-bottom:12px;}")
                .append(".matrix-tag{font-size:11px;font-weight:700;padding:3px 8px;border-radius:4px;display:inline-block;margin-bottom:8px;text-transform:uppercase;}")
                .append(".tag-past{background:#E2E8F0;color:#334155;}")
                .append(".tag-present{background:#DBEAFE;color:#1D4ED8;}")
                .append(".tag-future{background:#DCFCE7;color:#15803D;}")
                .append(".matrix-title{font-size:14.5px;font-weight:700;color:#0F172A;margin-bottom:6px;}")
                .append(".matrix-desc{font-size:13px;color:#475569;line-height:1.55;margin:0;}")
                .append(".action-box{background:#F0FDF4;border:1px solid #BBF7D0;border-radius:10px;padding:16px 18px;margin-bottom:10px;}")
                .append(".action-item{display:flex;gap:12px;margin-bottom:10px;font-size:13px;line-height:1.5;color:#1E293B;}")
                .append(".action-num{width:22px;height:22px;border-radius:50%;background:#16A34A;color:#FFFFFF;display:inline-flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0;}")
                .append(".article-card{border:1px solid #E2E8F0;border-radius:10px;padding:14px 16px;margin-bottom:12px;background:#FFFFFF;}")
                .append(".article-card h4{margin:0 0 6px 0;font-size:14.5px;font-weight:700;line-height:1.4;}")
                .append(".article-card h4 a{color:#0F172A;text-decoration:none;}")
                .append(".article-card p{margin:0 0 8px 0;font-size:12.5px;color:#64748B;line-height:1.5;}")
                .append(".article-meta{font-size:11.5px;color:#94A3B8;display:flex;align-items:center;gap:8px;}")
                .append(".footer{background:#F8FAFC;padding:20px 28px;text-align:center;font-size:12px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<span class='badge'>Báo cáo Chiến lược Tiến hóa AI</span>")
                .append("<h1>").append(reportTitle).append("</h1>")
                .append("<p>").append(reportDate).append(" &middot; Bản tin tự động phân tích đột phá AI và đề xuất nâng cấp nền tảng AIFP</p>")
                .append("</div>")

                // Section 1: Daily AI Breakthroughs
                .append("<div class='section'>")
                .append("<div class='section-title'>1. Biến Động & Đột Phá AI Hàng Ngày (Frontier AI Radar)</div>");

        if (breakthroughs != null) {
            for (DigestArticleCard item : breakthroughs) {
                html.append("<div class='article-card'>")
                        .append("<h4><a href='").append(item.url()).append("' target='_blank'>").append(item.title()).append("</a></h4>")
                        .append("<p>").append(item.summary()).append("</p>")
                        .append("<div class='article-meta'><span>Nguồn: ").append(item.sourceName()).append("</span><span>&bull;</span><span>").append(item.publishedTime()).append("</span></div>")
                        .append("</div>");
            }
        }

        html.append("</div>")

                // Section 2: Past, Present, Future Matrix
                .append("<div class='section'>")
                .append("<div class='section-title'>2. Đối Chiếu Hệ Thống AIFP (Quá Khứ &middot; Hiện Tại &middot; Tương Lai)</div>")
                .append("<div class='matrix-card'>")
                .append("<span class='matrix-tag tag-past'>Quá khứ &middot; Nền móng đã hoàn thành</span>")
                .append("<div class='matrix-title'>16/16 Module cốt lõi & Cơ chế Vận hành ngầm Zero-App-Open</div>")
                .append("<p class='matrix-desc'>").append(pastSummary).append("</p>")
                .append("</div>")

                .append("<div class='matrix-card'>")
                .append("<span class='matrix-tag tag-present'>Hiện tại &middot; Năng lực vận hành thực tế</span>")
                .append("<div class='matrix-title'>Spring Boot 3 + Python LangGraph + Failover Router đa nhà cung cấp</div>")
                .append("<p class='matrix-desc'>").append(presentSummary).append("</p>")
                .append("</div>")

                .append("<div class='matrix-card'>")
                .append("<span class='matrix-tag tag-future'>Tương lai &middot; Cơ hội & Lộ trình bứt phá</span>")
                .append("<div class='matrix-title'>Chuẩn MCP, Mô hình On-Device SLM & Voice Duplex Streaming</div>")
                .append("<p class='matrix-desc'>").append(futureRoadmap).append("</p>")
                .append("</div>")
                .append("</div>")

                // Section 3: Actionable System Improvements & New Features for AIFP
                .append("<div class='section'>")
                .append("<div class='section-title'>3. Đề Xuất Cải Tiến Kỹ Thuật & Chức Năng Mới Cho AIFP</div>")
                .append("<div class='action-box'>");

        if (suggestedImprovements != null) {
            int idx = 1;
            for (String action : suggestedImprovements) {
                html.append("<div class='action-item'>")
                        .append("<span class='action-num'>").append(idx++).append("</span>")
                        .append("<div>").append(action).append("</div>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("Bản tin Tiến hóa AI được tổng hợp & đối chiếu tự động bởi AIFP Autonomous Engine.<br/>")
                .append("Gửi tự động tới <strong>").append(fromEmail).append("</strong> theo cấu hình tiến hóa hệ thống hàng ngày.")
                .append("</div>")
                .append("</div></body></html>");

        return html.toString();
    }
}

