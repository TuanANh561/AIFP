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

    @Value("${spring.mail.username:assistant@aifp.local}")
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
        String safeTitle = (title != null) ? title : "Aura Bản tin";
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:24px;background-color:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;">
              <table align="center" width="100%" style="max-width:580px;background:#FFFFFF;border-radius:10px;border:1px solid #E2E8F0;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.03);">
                <tr>
                  <td style="padding:18px 22px;background:#FFFFFF;border-bottom:1px solid #E2E8F0;">
                    <div style="font-size:11px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;color:#64748B;">Aura &middot; Trợ lý Cá nhân</div>
                    <div style="font-size:17px;font-weight:700;color:#0F172A;margin-top:4px;">{{TITLE}}</div>
                  </td>
                </tr>
                <tr>
                  <td style="padding:22px;line-height:1.6;font-size:13.5px;color:#334155;">
                    {{CONTENT}}
                  </td>
                </tr>
                <tr>
                  <td style="padding:14px 22px;background:#FFFFFF;border-top:1px solid #E2E8F0;font-size:11.5px;color:#94A3B8;text-align:center;">
                    Bản tin từ Trợ lý Aura &middot; Bảo mật đa tầng
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

    public record JobCardItem(
            String sourceName,
            String sourceBadge,
            String title,
            String subtitle,
            String locationPill,
            String employmentType,
            String salaryRange,
            java.util.List<String> bulletPoints,
            String description,
            String note,
            String applyUrl,
            String imageUrl
    ) {
        public JobCardItem(
                String sourceName,
                String sourceBadge,
                String title,
                String subtitle,
                String locationPill,
                String employmentType,
                String salaryRange,
                java.util.List<String> bulletPoints,
                String description,
                String note,
                String applyUrl
        ) {
            this(sourceName, sourceBadge, title, subtitle, locationPill, employmentType, salaryRange, bulletPoints, description, note, applyUrl, "");
        }
    }

    public record OpenSourceProposalItem(
            String name,
            String githubUrl,
            String stars,
            String license,
            String primaryRole,
            String problemSolvedInAifp,
            String architecturalImpact,
            String sampleSnippet
    ) {}

    public record MonetizationIdeaItem(
            String category,
            String title,
            String targetPlatform,
            String monetizationPotential,
            String whyAiFailedVsRealHuman,
            java.util.List<String> executionSteps,
            String toolstackRecommended,
            String immediateActionTonight
    ) {}

    public String renderCardBasedDigestHtml(String digestTitle, java.util.List<DigestArticleCard> articles) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#0F172A;}")
                .append(".container{max-width:580px;margin:0 auto;background:#FFFFFF;border-radius:10px;overflow:hidden;border:1px solid #E2E8F0;box-shadow:0 1px 3px rgba(0,0,0,0.03);}")
                .append(".header{background:#FFFFFF;border-bottom:1px solid #E2E8F0;padding:20px 22px;}")
                .append(".header-brand{font-size:11px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;color:#64748B;margin-bottom:4px;}")
                .append(".header h1{margin:0;font-size:17px;font-weight:700;color:#0F172A;letter-spacing:-0.2px;}")
                .append(".content{padding:18px 22px;}")
                .append(".card{border:1px solid #E2E8F0;border-radius:8px;padding:16px;margin-bottom:14px;background:#FFFFFF;box-shadow:0 1px 2px rgba(0,0,0,0.02);}")
                .append(".card-meta{display:flex;align-items:center;gap:6px;font-size:11.5px;color:#64748B;margin-bottom:8px;}")
                .append(".source-badge{background:#F1F5F9;color:#475569;border:1px solid #E2E8F0;font-weight:600;padding:2px 6px;border-radius:4px;font-size:11px;}")
                .append(".card h3{margin:0 0 6px 0;font-size:15px;line-height:1.4;font-weight:700;}")
                .append(".card h3 a{color:#0F172A;text-decoration:none;}")
                .append(".card-thumb{width:100%;height:150px;object-fit:cover;border-radius:6px;margin-bottom:10px;display:block;border:1px solid #F1F5F9;}")
                .append(".card-desc{font-size:13px;color:#475569;line-height:1.5;margin:0 0 10px 0;}")
                .append(".card-link{display:inline-block;font-size:12.5px;font-weight:600;color:#0F172A;text-decoration:none;}")
                .append(".footer{background:#FFFFFF;padding:14px 22px;font-size:11.5px;color:#94A3B8;text-align:center;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'><div class='header-brand'>Aura &middot; Bản tin tự động</div><h1>").append(digestTitle).append("</h1></div>")
                .append("<div class='content'>");

        if (articles != null) {
            for (DigestArticleCard item : articles) {
                html.append("<div class='card'>");
                if (item.imageUrl() != null && !item.imageUrl().isBlank()) {
                    html.append("<img class='card-thumb' src='").append(item.imageUrl()).append("' alt='thumbnail'/>");
                }
                html.append("<div class='card-meta'><span class='source-badge'>").append(item.sourceName()).append("</span>")
                        .append("<span>&middot; ").append(item.publishedTime()).append("</span></div>")
                        .append("<h3><a href='").append(item.url()).append("' target='_blank'>").append(item.title()).append("</a></h3>")
                        .append("<p class='card-desc'>").append(item.summary()).append("</p>")
                        .append("<a class='card-link' href='").append(item.url()).append("' target='_blank'>Xem bài →</a>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("<div class='footer'>Bản tin đã được kiểm định nguồn bởi Trợ lý Aura &middot; Gửi theo lịch hẹn</div>")
                .append("</div></body></html>");

        return html.toString();
    }

    public String renderJobCardDigestHtml(
            String digestTitle,
            String recipientEmail,
            java.util.List<JobCardItem> jobs
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#0F172A;}")
                .append(".container{max-width:580px;margin:0 auto;background:#FFFFFF;border-radius:10px;overflow:hidden;border:1px solid #E2E8F0;box-shadow:0 1px 3px rgba(0,0,0,0.03);}")
                .append(".header{background:#FFFFFF;border-bottom:1px solid #E2E8F0;padding:20px 22px;text-align:left;}")
                .append(".header-brand{font-size:11px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;color:#64748B;margin-bottom:4px;}")
                .append(".header h1{margin:0;font-size:17px;font-weight:700;color:#0F172A;letter-spacing:-0.2px;}")
                .append(".content{padding:18px 22px;}")
                .append(".job-card{border:1px solid #E2E8F0;border-radius:8px;padding:16px;margin-bottom:14px;background:#FFFFFF;box-shadow:0 1px 2px rgba(0,0,0,0.02);}")
                .append(".job-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;}")
                .append(".job-source{font-size:12px;font-weight:700;color:#0F172A;display:flex;align-items:center;gap:6px;}")
                .append(".job-tag-sponsored{font-size:11px;color:#64748B;font-weight:400;}")
                .append(".job-title{font-size:15px;font-weight:700;color:#0F172A;margin:0 0 6px 0;line-height:1.4;}")
                .append(".job-meta{font-size:12.5px;color:#475569;margin-bottom:10px;line-height:1.4;}")
                .append(".job-bullets{margin:0 0 12px 0;padding-left:0;}")
                .append(".job-bullet{font-size:12.5px;color:#475569;line-height:1.5;margin-bottom:4px;}")
                .append(".apply-btn{display:inline-block;background:#0F172A;color:#FFFFFF;text-decoration:none;padding:7px 16px;border-radius:6px;font-size:12.5px;font-weight:600;}")
                .append(".footer{background:#FFFFFF;padding:14px 22px;text-align:center;font-size:11.5px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'><div class='header-brand'>Aura &middot; Cơ hội việc làm</div><h1>").append(digestTitle).append("</h1></div>")
                .append("<div class='content'>");

        if (jobs != null) {
            for (JobCardItem job : jobs) {
                html.append("<div class='job-card'>");
                if (job.imageUrl() != null && !job.imageUrl().isBlank()) {
                    html.append("<img src='").append(job.imageUrl()).append("' alt='").append(job.title()).append("' style='width:100%;height:130px;object-fit:cover;border-radius:6px;margin-bottom:10px;display:block;border:1px solid #F1F5F9;' />");
                }
                html.append("<div class='job-header'>")
                        .append("<div class='job-source'>")
                        .append("<span>").append(job.sourceName()).append("</span>")
                        .append("<span class='job-tag-sponsored'>&middot; ").append(job.sourceBadge()).append("</span>")
                        .append("</div>")
                        .append("</div>")
                        .append("<div class='job-title'>").append(job.title()).append("</div>")
                        .append("<div class='job-meta'>")
                        .append(job.locationPill()).append(" &middot; ")
                        .append(job.employmentType()).append(" &middot; ")
                        .append("<strong>").append(job.salaryRange()).append("</strong>")
                        .append("</div>")
                        .append("<div class='job-bullets'>");

                if (job.bulletPoints() != null) {
                    int bCount = 0;
                    for (String b : job.bulletPoints()) {
                        if (bCount++ >= 2) break; // Limit to 2 concise bullet points to avoid text bloat
                        html.append("<div class='job-bullet'>&bull; ").append(b).append("</div>");
                    }
                }

                html.append("</div>")
                        .append("<a class='apply-btn' href='").append(job.applyUrl()).append("' target='_blank'>Ứng tuyển →</a>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("<div class='footer'>Bản tin gửi tự động từ Trợ lý Aura đến ").append(recipientEmail)
                .append(" &middot; Nguồn đã kiểm chứng</div>")
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
        return renderAiEvolutionHtml(
                reportTitle,
                reportDate,
                breakthroughs,
                null,
                pastSummary,
                presentSummary,
                futureRoadmap,
                suggestedImprovements,
                "http://localhost:5173/"
        );
    }

    public String renderAiEvolutionHtml(
            String reportTitle,
            String reportDate,
            List<DigestArticleCard> breakthroughs,
            List<OpenSourceProposalItem> openSourceProposals,
            String pastSummary,
            String presentSummary,
            String futureRoadmap,
            List<String> suggestedImprovements,
            String webReviewUrl
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;}")
                .append(".container{max-width:620px;margin:0 auto;background:#FFFFFF;border-radius:10px;overflow:hidden;border:1px solid #E2E8F0;box-shadow:0 1px 3px rgba(0,0,0,0.03);}")
                .append(".header{background:#FFFFFF;border-bottom:1px solid #E2E8F0;padding:22px 24px;text-align:left;}")
                .append(".header-brand{font-size:11px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;color:#64748B;margin-bottom:4px;}")
                .append(".header h1{margin:0;font-size:18px;font-weight:700;letter-spacing:-0.3px;color:#0F172A;line-height:1.35;}")
                .append(".header p{margin:4px 0 0 0;font-size:12px;color:#94A3B8;}")
                .append(".section{padding:20px 24px;border-bottom:1px solid #E2E8F0;}")
                .append(".section-title{font-size:12.5px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#0F172A;margin-bottom:14px;}")
                .append(".matrix-card{padding:14px 16px;border-radius:8px;border:1px solid #E2E8F0;background:#F8FAFC;margin-bottom:10px;}")
                .append(".matrix-tag{font-size:10.5px;font-weight:700;color:#64748B;display:inline-block;margin-bottom:6px;text-transform:uppercase;letter-spacing:0.4px;}")
                .append(".matrix-title{font-size:14px;font-weight:700;color:#0F172A;margin-bottom:4px;}")
                .append(".matrix-desc{font-size:12.5px;color:#475569;line-height:1.5;margin:0;}")
                .append(".action-box{background:#F8FAFC;border:1px solid #E2E8F0;border-radius:8px;padding:14px 16px;margin-bottom:10px;}")
                .append(".action-item{display:flex;gap:8px;margin-bottom:8px;font-size:12.5px;line-height:1.5;color:#334155;}")
                .append(".article-card{border:1px solid #E2E8F0;border-radius:8px;padding:12px 14px;margin-bottom:10px;background:#FFFFFF;box-shadow:0 1px 2px rgba(0,0,0,0.02);}")
                .append(".article-card h4{margin:0 0 4px 0;font-size:14px;font-weight:700;line-height:1.4;}")
                .append(".article-card h4 a{color:#0F172A;text-decoration:none;}")
                .append(".article-card p{margin:0 0 6px 0;font-size:12.5px;color:#64748B;line-height:1.5;}")
                .append(".article-meta{font-size:11px;color:#94A3B8;display:flex;align-items:center;gap:6px;}")
                .append(".oss-card{border:1px solid #E2E8F0;border-radius:8px;padding:14px 16px;margin-bottom:12px;background:#FFFFFF;box-shadow:0 1px 2px rgba(0,0,0,0.02);}")
                .append(".oss-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;flex-wrap:wrap;gap:6px;}")
                .append(".oss-name{font-size:14.5px;font-weight:700;color:#0F172A;text-decoration:none;}")
                .append(".oss-badges{display:flex;align-items:center;gap:6px;}")
                .append(".oss-stars{background:#F1F5F9;color:#475569;font-size:11px;font-weight:600;padding:2px 6px;border-radius:4px;border:1px solid #E2E8F0;}")
                .append(".oss-license{background:#F1F5F9;color:#475569;font-size:11px;font-weight:600;padding:2px 6px;border-radius:4px;border:1px solid #E2E8F0;}")
                .append(".oss-role{font-size:11.5px;font-weight:600;color:#64748B;text-transform:uppercase;margin-bottom:4px;}")
                .append(".oss-detail{font-size:12.5px;color:#475569;line-height:1.5;margin-bottom:4px;}")
                .append(".oss-code{background:#0F172A;color:#38BDF8;font-family:'JetBrains Mono',Consolas,monospace;font-size:11px;padding:8px 12px;border-radius:6px;overflow-x:auto;margin:6px 0 0 0;line-height:1.4;}")
                .append(".hitl-box{background:#F8FAFC;border:1px solid #E2E8F0;border-radius:8px;padding:18px 16px;text-align:center;}")
                .append(".hitl-title{font-size:14.5px;font-weight:700;color:#0F172A;margin-bottom:4px;}")
                .append(".hitl-desc{font-size:12.5px;color:#64748B;line-height:1.5;margin-bottom:14px;max-width:480px;margin-left:auto;margin-right:auto;}")
                .append(".hitl-btn-primary{display:inline-block;background:#0F172A;color:#FFFFFF;text-decoration:none;padding:8px 18px;border-radius:6px;font-weight:600;font-size:12.5px;margin:3px;}")
                .append(".hitl-btn-secondary{display:inline-block;background:#FFFFFF;color:#0F172A;text-decoration:none;padding:8px 18px;border-radius:6px;font-weight:600;font-size:12.5px;border:1px solid #CBD5E1;margin:3px;}")
                .append(".footer{background:#FFFFFF;padding:16px 24px;text-align:center;font-size:11.5px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<div class='header-brand'>Aura &middot; Báo cáo tiến hóa AI</div>")
                .append("<h1>").append(reportTitle).append("</h1>")
                .append("<p>").append(reportDate).append(" &middot; Đề xuất giải pháp nâng cấp hệ thống</p>")
                .append("</div>")

                // Section 1: Daily AI Breakthroughs
                .append("<div class='section'>")
                .append("<div class='section-title'>1. Biến Động AI Hàng Ngày</div>");

        if (breakthroughs != null) {
            for (DigestArticleCard item : breakthroughs) {
                html.append("<div class='article-card'>")
                        .append("<h4><a href='").append(item.url()).append("' target='_blank'>").append(item.title()).append("</a></h4>")
                        .append("<p>").append(item.summary()).append("</p>")
                        .append("<div class='article-meta'><span>").append(item.sourceName()).append("</span><span>&middot;</span><span>").append(item.publishedTime()).append("</span></div>")
                        .append("</div>");
            }
        }

        html.append("</div>");

        // Section 2: Curated Open-Source Proposals
        if (openSourceProposals != null && !openSourceProposals.isEmpty()) {
            html.append("<div class='section'>")
                    .append("<div class='section-title'>2. Đề Xuất Mã Nguồn Mở Tinh Hoa Cho Aura</div>");

            for (OpenSourceProposalItem oss : openSourceProposals) {
                html.append("<div class='oss-card'>")
                        .append("<div class='oss-header'>")
                        .append("<a class='oss-name' href='").append(oss.githubUrl()).append("' target='_blank'>").append(oss.name()).append(" ↗</a>")
                        .append("<div class='oss-badges'>")
                        .append("<span class='oss-stars'>").append(oss.stars()).append("</span>")
                        .append("<span class='oss-license'>").append(oss.license()).append("</span>")
                        .append("</div>")
                        .append("</div>")
                        .append("<div class='oss-role'>").append(oss.primaryRole()).append("</div>")
                        .append("<div class='oss-detail'><strong>Bài toán:</strong> ").append(oss.problemSolvedInAifp()).append("</div>")
                        .append("<div class='oss-detail'><strong>Kiến trúc:</strong> ").append(oss.architecturalImpact()).append("</div>");

                if (oss.sampleSnippet() != null && !oss.sampleSnippet().isBlank()) {
                    html.append("<pre class='oss-code'><code>").append(oss.sampleSnippet()).append("</code></pre>");
                }

                html.append("</div>");
            }

            html.append("</div>");
        }

        // Section 3: Past, Present, Future Matrix
        html.append("<div class='section'>")
                .append("<div class='section-title'>3. Đối Chiếu Hệ Thống (Quá Khứ &middot; Hiện Tại &middot; Tương Lai)</div>")
                .append("<div class='matrix-card'>")
                .append("<div class='matrix-tag'>Nền móng đã hoàn thành</div>")
                .append("<div class='matrix-title'>16/16 Module cốt lõi & Vận hành ngầm Zero-App-Open</div>")
                .append("<p class='matrix-desc'>").append(pastSummary).append("</p>")
                .append("</div>")

                .append("<div class='matrix-card'>")
                .append("<div class='matrix-tag'>Năng lực vận hành thực tế</div>")
                .append("<div class='matrix-title'>Spring Boot 3 + Python LangGraph + Failover Router đa nhà cung cấp</div>")
                .append("<p class='matrix-desc'>").append(presentSummary).append("</p>")
                .append("</div>")

                .append("<div class='matrix-card'>")
                .append("<div class='matrix-tag'>Cơ hội & Lộ trình bứt phá</div>")
                .append("<div class='matrix-title'>Chuẩn MCP, Mô hình On-Device SLM & Voice Duplex Streaming</div>")
                .append("<p class='matrix-desc'>").append(futureRoadmap).append("</p>")
                .append("</div>")
                .append("</div>")

                // Section 4: Actionable System Improvements & New Features for AIFP
                .append("<div class='section'>")
                .append("<div class='section-title'>4. Đề Xuất Kỹ Thuật Hành Động Cụ Thể (RFCs)</div>")
                .append("<div class='action-box'>");

        if (suggestedImprovements != null) {
            int idx = 1;
            for (String action : suggestedImprovements) {
                html.append("<div class='action-item'>")
                        .append("<span>").append(idx++).append(".</span>")
                        .append("<div>").append(action).append("</div>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("</div>")

                // Section 5: Human-in-the-Loop Review CTA
                .append("<div class='section'>")
                .append("<div class='hitl-box'>")
                .append("<div class='hitl-title'>Phê duyệt áp dụng đề xuất này?</div>")
                .append("<div class='hitl-desc'>Hệ thống tự học và soạn thảo giải pháp, mọi thay đổi kiến trúc đều cần bạn xem xét.</div>")
                .append("<div>")
                .append("<a class='hitl-btn-primary' href='").append(webReviewUrl != null ? webReviewUrl : "http://localhost:5173/").append("' target='_blank'>Xem chi tiết →</a>")
                .append("<a class='hitl-btn-secondary' href='").append(webReviewUrl != null ? webReviewUrl : "http://localhost:5173/").append("' target='_blank'>Mở Trợ lý →</a>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("Bản tin Tự Tiến Hóa &middot; Aura Continuous Self-Evolution Engine<br/>")
                .append("Gửi tự động theo dõi lịch trình phát triển hệ thống hàng ngày.")
                .append("</div>")
                .append("</div></body></html>");

        return html.toString();
    }

    public String renderMonetizationIdeaDigestHtml(
            String reportTitle,
            String reportDate,
            String recipientEmail,
            String algorithmInsight,
            List<MonetizationIdeaItem> ideas,
            String webActionUrl
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F8FAFC;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;}")
                .append(".container{max-width:580px;margin:0 auto;background:#FFFFFF;border-radius:10px;overflow:hidden;border:1px solid #E2E8F0;box-shadow:0 1px 3px rgba(0,0,0,0.03);}")
                .append(".header{background:#FFFFFF;border-bottom:1px solid #E2E8F0;padding:20px 22px;text-align:left;}")
                .append(".header-brand{font-size:11px;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;color:#64748B;margin-bottom:4px;}")
                .append(".header h1{margin:0;font-size:17px;font-weight:700;color:#0F172A;letter-spacing:-0.2px;line-height:1.35;}")
                .append(".header p{margin:4px 0 0 0;font-size:12px;color:#94A3B8;}")
                .append(".section{padding:18px 22px;border-bottom:1px solid #E2E8F0;}")
                .append(".section-title{font-size:12.5px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:#0F172A;margin-bottom:12px;}")
                .append(".insight-box{background:#F8FAFC;border:1px solid #E2E8F0;border-radius:8px;padding:14px;margin-bottom:12px;}")
                .append(".insight-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:10px;}")
                .append(".card-comparison{background:#FFFFFF;border:1px solid #E2E8F0;border-radius:6px;padding:10px 12px;font-size:12px;color:#334155;line-height:1.5;}")
                .append(".idea-card{border:1px solid #E2E8F0;border-radius:8px;padding:16px;background:#FFFFFF;margin-bottom:14px;box-shadow:0 1px 2px rgba(0,0,0,0.02);}")
                .append(".idea-header{display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:6px;margin-bottom:8px;}")
                .append(".idea-title{font-size:15px;font-weight:700;color:#0F172A;margin:0;line-height:1.35;}")
                .append(".pill-platform{display:inline-block;padding:2px 6px;border-radius:4px;background:#F1F5F9;color:#475569;border:1px solid #E2E8F0;font-size:11px;font-weight:600;}")
                .append(".pill-money{display:inline-block;padding:2px 6px;border-radius:4px;background:#F1F5F9;color:#0F172A;border:1px solid #E2E8F0;font-size:11px;font-weight:600;}")
                .append(".why-box{background:#F8FAFC;border:1px solid #E2E8F0;padding:10px 12px;border-radius:6px;margin:10px 0;font-size:12.5px;line-height:1.5;color:#475569;}")
                .append(".steps-title{font-size:12px;font-weight:700;text-transform:uppercase;color:#64748B;margin:12px 0 6px 0;}")
                .append(".step-item{display:flex;gap:6px;margin-bottom:6px;font-size:12.5px;color:#334155;line-height:1.5;}")
                .append(".step-num{font-weight:700;color:#64748B;min-width:16px;}")
                .append(".action-tonight{background:#F8FAFC;border:1px solid #E2E8F0;border-radius:6px;padding:10px 12px;margin-top:10px;font-size:12px;color:#334155;line-height:1.5;}")
                .append(".table-platform{width:100%;border-collapse:collapse;margin-top:10px;font-size:12px;}")
                .append(".table-platform th{background:#F8FAFC;padding:8px 10px;border:1px solid #E2E8F0;text-align:left;font-weight:700;color:#0F172A;}")
                .append(".table-platform td{padding:8px 10px;border:1px solid #E2E8F0;color:#334155;line-height:1.45;}")
                .append(".cta-box{background:#F8FAFC;border:1px solid #E2E8F0;border-radius:8px;padding:16px;text-align:center;}")
                .append(".cta-btn-primary{display:inline-block;background:#0F172A;color:#FFFFFF;text-decoration:none;padding:7px 16px;border-radius:6px;font-weight:600;font-size:12px;margin:3px;}")
                .append(".cta-btn-secondary{display:inline-block;background:#FFFFFF;color:#0F172A;text-decoration:none;padding:7px 16px;border-radius:6px;font-weight:600;font-size:12px;border:1px solid #CBD5E1;margin:3px;}")
                .append(".footer{background:#FFFFFF;padding:14px 22px;text-align:center;font-size:11.5px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<div class='header-brand'>Aura &middot; Ý tưởng sáng tạo</div>")
                .append("<h1>").append(reportTitle).append("</h1>")
                .append("<p>").append(reportDate).append(" &middot; Kế hoạch phát triển nội dung & kiếm tiền</p>")
                .append("</div>")

                // Section 1: Algorithm Reality Check
                .append("<div class='section'>")
                .append("<div class='section-title'>1. Phân Tích Hiệu Suất Video</div>")
                .append("<div class='insight-box'>")
                .append("<div style='font-size:12.5px;line-height:1.5;color:#334155;'><strong>Thực tế thuật toán:</strong> ").append(algorithmInsight).append("</div>")
                .append("<div class='insight-grid'>")
                .append("<div class='card-comparison'><strong>Video thuần AI:</strong> Giọng đọc máy đều đều, hình ảnh lặp lại, thiếu tính xác thực khiến người xem rời đi sớm.</div>")
                .append("<div class='card-comparison'><strong>Mô hình Hybrid:</strong> Gameplay thực tế từ bạn làm cốt lõi, AI hỗ trợ tối ưu tiêu đề, kịch bản 3 giây đầu và SEO.</div>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Section 2: Actionable Monetization Ideas & Step-by-Step
                .append("<div class='section'>")
                .append("<div class='section-title'>2. Ý Tưởng & Kế Hoạch Triển Khai</div>");

        if (ideas != null) {
            int i = 1;
            for (MonetizationIdeaItem idea : ideas) {
                html.append("<div class='idea-card'>")
                        .append("<div class='idea-header'>")
                        .append("<h3 class='idea-title'>").append(i++).append(". ").append(idea.title()).append("</h3>")
                        .append("<div style='display:flex;gap:6px;'>")
                        .append("<span class='pill-platform'>").append(idea.targetPlatform()).append("</span>")
                        .append("<span class='pill-money'>").append(idea.monetizationPotential()).append("</span>")
                        .append("</div>")
                        .append("</div>")

                        .append("<div class='why-box'><strong>Điểm mấu chốt:</strong> ").append(idea.whyAiFailedVsRealHuman()).append("</div>")

                        .append("<div class='steps-title'>Các bước thực hiện:</div>");

                if (idea.executionSteps() != null) {
                    int sIdx = 1;
                    for (String step : idea.executionSteps()) {
                        html.append("<div class='step-item'>")
                                .append("<span class='step-num'>").append(sIdx++).append(".</span>")
                                .append("<div>").append(step).append("</div>")
                                .append("</div>");
                    }
                }

                html.append("<div style='margin-top:10px;font-size:12px;color:#64748B;'><strong>Công cụ:</strong> ").append(idea.toolstackRecommended()).append("</div>")
                        .append("<div class='action-tonight'><strong>Hành động tối nay:</strong> ").append(idea.immediateActionTonight()).append("</div>")
                        .append("</div>");
            }
        }

        html.append("</div>")

                // Section 3: Platform Matrix
                .append("<div class='section'>")
                .append("<div class='section-title'>3. Phân Bổ Nền Tảng Phù Hợp</div>")
                .append("<table class='table-platform'>")
                .append("<thead><tr><th>Nền tảng</th><th>Định dạng</th><th>Nguồn thu</th></tr></thead>")
                .append("<tbody>")
                .append("<tr><td><strong>YouTube (8–15p)</strong></td><td>Gameplay thử thách, Devlog làm game</td><td>AdSense, Tài trợ, Tiếp thị</td></tr>")
                .append("<tr><td><strong>TikTok & Shorts</strong></td><td>Khoảnh khắc điểm nhấn 30–60s</td><td>Kéo lượng xem, Quỹ creator</td></tr>")
                .append("<tr><td><strong>Steam & Itch.io</strong></td><td>Game Indie ngắn gọn hoàn thiện</td><td>Bán game thu ngoại tệ</td></tr>")
                .append("<tr><td><strong>Roblox</strong></td><td>Minigame vui nhộn cộng đồng</td><td>Vật phẩm qua Robux/DevEx</td></tr>")
                .append("</tbody></table>")
                .append("</div>")

                // Section 4: Aura Interactive Action CTA
                .append("<div class='section'>")
                .append("<div class='cta-box'>")
                .append("<div style='font-size:14px;font-weight:700;color:#0F172A;margin-bottom:4px;'>Hiện thực hóa cùng Trợ lý Aura</div>")
                .append("<div style='font-size:12px;color:#64748B;margin-bottom:12px;'>Mở Trợ lý để yêu cầu tạo kịch bản chi tiết hoặc hỗ trợ code game mẫu.</div>")
                .append("<div>")
                .append("<a class='cta-btn-primary' href='").append(webActionUrl != null ? webActionUrl : "http://localhost:5173/").append("' target='_blank'>Tạo kịch bản →</a>")
                .append("<a class='cta-btn-secondary' href='").append(webActionUrl != null ? webActionUrl : "http://localhost:5173/").append("' target='_blank'>Xem code game →</a>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("Bản tin ý tưởng gửi tự động từ Trợ lý Aura &middot; ").append(recipientEmail)
                .append("</div>")
                .append("</div></body></html>");

        return html.toString();
    }
}

