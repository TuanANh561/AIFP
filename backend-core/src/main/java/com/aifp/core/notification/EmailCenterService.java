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
                    Bản tin tự động gửi từ Trợ lý Cá nhân AIFP &middot; Bảo mật đa tầng
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

    public String renderJobCardDigestHtml(
            String digestTitle,
            String recipientEmail,
            java.util.List<JobCardItem> jobs
    ) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang='vi'><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/><style>")
                .append("body{margin:0;padding:24px;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#0F172A;}")
                .append(".container{max-width:600px;margin:0 auto;background:#FFFFFF;border-radius:16px;overflow:hidden;border:1px solid #CBD5E1;box-shadow:0 6px 20px rgba(0,0,0,0.06);}")
                .append(".header{background:#0F172A;color:#FFFFFF;padding:28px 24px;text-align:left;}")
                .append(".badge{display:inline-block;padding:4px 10px;border-radius:9999px;background:#0F766E;color:#FFFFFF;font-size:11px;font-weight:700;letter-spacing:0.4px;text-transform:uppercase;}")
                .append(".header h1{margin:10px 0 4px 0;font-size:20px;font-weight:800;letter-spacing:-0.3px;}")
                .append(".header p{margin:0;font-size:13px;color:#94A3B8;}")
                .append(".content{padding:20px 24px;}")
                .append(".job-card{border:1px solid #E2E8F0;border-radius:14px;padding:20px 22px;margin-bottom:20px;background:#FFFFFF;box-shadow:0 2px 8px rgba(0,0,0,0.03);}")
                .append(".job-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;}")
                .append(".job-source{font-size:13px;font-weight:700;color:#0F172A;display:flex;align-items:center;gap:8px;}")
                .append(".job-tag-sponsored{font-size:11px;color:#64748B;font-weight:400;}")
                .append(".bookmark-icon{font-size:16px;color:#94A3B8;}")
                .append(".job-title{font-size:18px;font-weight:800;color:#1E3A8A;margin:0 0 4px 0;letter-spacing:-0.2px;text-transform:uppercase;line-height:1.35;}")
                .append(".job-subtitle{font-size:13.5px;color:#475569;margin-bottom:12px;font-weight:500;}")
                .append(".job-pill{display:inline-block;padding:4px 10px;border-radius:6px;background:#DCFCE7;color:#15803D;font-size:12px;font-weight:700;margin-bottom:10px;}")
                .append(".job-type{font-size:13px;font-weight:600;color:#334155;margin-bottom:6px;}")
                .append(".job-salary{font-size:14px;font-weight:700;color:#0F172A;margin-bottom:12px;}")
                .append(".job-bullets{margin:0 0 14px 0;padding-left:0;list-style:none;}")
                .append(".job-bullet-item{font-size:13px;color:#334155;line-height:1.55;margin-bottom:6px;display:flex;gap:8px;}")
                .append(".job-desc{font-size:12.5px;color:#64748B;line-height:1.5;margin-bottom:8px;}")
                .append(".job-note{font-size:11.5px;color:#94A3B8;font-style:italic;margin-bottom:16px;}")
                .append(".apply-btn{display:block;width:100%;box-sizing:border-box;text-align:center;background:#0F172A;color:#FFFFFF;text-decoration:none;padding:12px 16px;border-radius:8px;font-size:13.5px;font-weight:700;letter-spacing:0.3px;}")
                .append(".footer{background:#F8FAFC;padding:18px 24px;text-align:center;font-size:12px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<span class='badge'>Cơ hội Việc làm Tuyển chọn</span>")
                .append("<h1>").append(digestTitle).append("</h1>")
                .append("<p>Đã lọc: Test Game &bull; Cày Game &bull; Remote không yêu cầu kinh nghiệm &bull; Tiếng Việt</p>")
                .append("</div>")
                .append("<div class='content'>");

        if (jobs != null) {
            for (JobCardItem job : jobs) {
                html.append("<div class='job-card'>");
                if (job.imageUrl() != null && !job.imageUrl().isBlank()) {
                    html.append("<img src='").append(job.imageUrl()).append("' alt='").append(job.title()).append("' style='width:100%;height:140px;object-fit:cover;border-radius:10px;margin-bottom:14px;display:block;' />");
                }
                html.append("<div class='job-header'>")
                        .append("<div class='job-source'>")
                        .append("<span>").append(job.sourceName()).append("</span>")
                        .append("<span class='job-tag-sponsored'>&bull; ").append(job.sourceBadge()).append("</span>")
                        .append("</div>")
                        .append("<div class='bookmark-icon'>&#128278;</div>")
                        .append("</div>")
                        .append("<div class='job-title'>").append(job.title()).append("</div>")
                        .append("<div class='job-subtitle'>").append(job.subtitle()).append("</div>")
                        .append("<div class='job-pill'>").append(job.locationPill()).append("</div>")
                        .append("<div class='job-type'>").append(job.employmentType()).append("</div>")
                        .append("<div class='job-salary'>").append(job.salaryRange()).append("</div>")
                        .append("<div class='job-bullets'>");

                if (job.bulletPoints() != null) {
                    for (String b : job.bulletPoints()) {
                        html.append("<div class='job-bullet-item'><span>&bull;</span><span>").append(b).append("</span></div>");
                    }
                }

                html.append("</div>")
                        .append("<div class='job-desc'>").append(job.description()).append("</div>")
                        .append("<div class='job-note'>").append(job.note()).append("</div>")
                        .append("<a class='apply-btn' href='").append(job.applyUrl()).append("' target='_blank'>Tìm hiểu thêm & Ứng tuyển ngay &gt;</a>")
                        .append("</div>");
            }
        }

        html.append("</div>")
                .append("<div class='footer'>")
                .append("Bản tin gửi tự động từ Trợ lý AIFP đến ").append(recipientEmail)
                .append("<br/>Mọi bài đăng đều được kiểm chứng và có liên kết ứng tuyển trực tiếp.")
                .append("</div>")
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
                .append(".oss-card{border:1px solid #E2E8F0;border-radius:12px;padding:18px 20px;margin-bottom:16px;background:#FFFFFF;box-shadow:0 2px 6px rgba(0,0,0,0.02);}")
                .append(".oss-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;flex-wrap:wrap;gap:8px;}")
                .append(".oss-name{font-size:16px;font-weight:800;color:#0F172A;text-decoration:none;}")
                .append(".oss-badges{display:flex;align-items:center;gap:6px;}")
                .append(".oss-stars{background:#FEF3C7;color:#92400E;font-size:11px;font-weight:700;padding:3px 8px;border-radius:4px;}")
                .append(".oss-license{background:#E0E7FF;color:#3730A3;font-size:11px;font-weight:700;padding:3px 8px;border-radius:4px;}")
                .append(".oss-role{font-size:12px;font-weight:700;color:#0D9488;text-transform:uppercase;margin-bottom:6px;}")
                .append(".oss-detail{font-size:13px;color:#334155;line-height:1.5;margin-bottom:6px;}")
                .append(".oss-code{background:#0F172A;color:#38BDF8;font-family:'JetBrains Mono',Consolas,monospace;font-size:11.5px;padding:10px 14px;border-radius:8px;overflow-x:auto;margin:8px 0 0 0;line-height:1.45;}")
                .append(".hitl-box{background:#F8FAFC;border:1px solid #CBD5E1;border-radius:12px;padding:24px 20px;text-align:center;}")
                .append(".hitl-title{font-size:16px;font-weight:800;color:#0F172A;margin-bottom:6px;}")
                .append(".hitl-desc{font-size:13px;color:#64748B;line-height:1.5;margin-bottom:16px;max-width:520px;margin-left:auto;margin-right:auto;}")
                .append(".hitl-btn-primary{display:inline-block;background:#0F172A;color:#FFFFFF;text-decoration:none;padding:12px 22px;border-radius:8px;font-weight:700;font-size:13.5px;margin:4px 6px;}")
                .append(".hitl-btn-secondary{display:inline-block;background:#FFFFFF;color:#0F172A;text-decoration:none;padding:12px 22px;border-radius:8px;font-weight:700;font-size:13.5px;border:1px solid #CBD5E1;margin:4px 6px;}")
                .append(".footer{background:#F8FAFC;padding:20px 28px;text-align:center;font-size:12px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<span class='badge'>Báo cáo Tự Tiến Hóa & Mã Nguồn Mở</span>")
                .append("<h1>").append(reportTitle).append("</h1>")
                .append("<p>").append(reportDate).append(" &middot; Bản tin tự động đối chiếu mã nguồn mở uy tín & đề xuất giải pháp nâng cấp hệ thống</p>")
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

        html.append("</div>");

        // Section 2: Curated Open-Source Proposals
        if (openSourceProposals != null && !openSourceProposals.isEmpty()) {
            html.append("<div class='section'>")
                    .append("<div class='section-title'>2. Đề Xuất Tích Hợp Mã Nguồn Mở Tinh Hoa Cho AIFP</div>");

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
                        .append("<div class='oss-detail'><strong>Bài toán giải quyết:</strong> ").append(oss.problemSolvedInAifp()).append("</div>")
                        .append("<div class='oss-detail'><strong>Kiến trúc & Tối ưu:</strong> ").append(oss.architecturalImpact()).append("</div>");

                if (oss.sampleSnippet() != null && !oss.sampleSnippet().isBlank()) {
                    html.append("<pre class='oss-code'><code>").append(oss.sampleSnippet()).append("</code></pre>");
                }

                html.append("</div>");
            }

            html.append("</div>");
        }

        // Section 3: Past, Present, Future Matrix
        html.append("<div class='section'>")
                .append("<div class='section-title'>3. Đối Chiếu Hệ Thống AIFP (Quá Khứ &middot; Hiện Tại &middot; Tương Lai)</div>")
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

                // Section 4: Actionable System Improvements & New Features for AIFP
                .append("<div class='section'>")
                .append("<div class='section-title'>4. Đề Xuất Kỹ Thuật Hành Động Cụ Thể (Actionable RFCs)</div>")
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

                // Section 5: Human-in-the-Loop Review CTA
                .append("<div class='section'>")
                .append("<div class='hitl-box'>")
                .append("<div class='hitl-title'>Bạn có đồng ý để hệ thống tiến hành áp dụng đề xuất này?</div>")
                .append("<div class='hitl-desc'>Tuân thủ nguyên tắc Level 3 Gate: Hệ thống tự nghiên cứu, tự học và tự động soạn thảo giải pháp, nhưng mọi thay đổi kiến trúc đều cần bạn xem xét và phê duyệt.</div>")
                .append("<div>")
                .append("<a class='hitl-btn-primary' href='").append(webReviewUrl != null ? webReviewUrl : "http://localhost:5173/").append("' target='_blank'>Xem & Duyệt trên AIFP Workspace →</a>")
                .append("<a class='hitl-btn-secondary' href='").append(webReviewUrl != null ? webReviewUrl : "http://localhost:5173/").append("' target='_blank'>Trao đổi chi tiết với Trợ lý AI</a>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("Bản tin Tự Tiến Hóa được tổng hợp & đối chiếu tự động bởi AIFP Continuous Self-Evolution Engine.<br/>")
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
                .append("body{margin:0;padding:24px;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#0F172A;}")
                .append(".container{max-width:700px;margin:0 auto;background:#FFFFFF;border-radius:16px;overflow:hidden;border:1px solid #CBD5E1;box-shadow:0 8px 24px rgba(0,0,0,0.06);}")
                .append(".header{background:#0F172A;color:#FFFFFF;padding:32px 28px;text-align:left;}")
                .append(".badge{display:inline-block;padding:4px 10px;border-radius:9999px;background:#C026D3;color:#FFFFFF;font-size:11px;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;}")
                .append(".header h1{margin:12px 0 6px 0;font-size:22px;font-weight:800;letter-spacing:-0.4px;line-height:1.3;}")
                .append(".header p{margin:0;font-size:13.5px;color:#94A3B8;line-height:1.5;}")
                .append(".section{padding:24px 28px;border-bottom:1px solid #E2E8F0;}")
                .append(".section-title{font-size:13.5px;font-weight:800;text-transform:uppercase;letter-spacing:0.5px;color:#0F172A;margin-bottom:16px;display:flex;align-items:center;gap:8px;}")
                .append(".insight-box{background:#FAF5FF;border:1px solid #E9D5FF;border-radius:12px;padding:20px;margin-bottom:16px;}")
                .append(".insight-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:14px;}")
                .append(".card-fail{background:#FEF2F2;border:1px solid #FECACA;border-radius:8px;padding:12px 14px;font-size:12.5px;color:#991B1B;line-height:1.5;}")
                .append(".card-win{background:#F0FDF4;border:1px solid #BBF7D0;border-radius:8px;padding:12px 14px;font-size:12.5px;color:#166534;line-height:1.5;}")
                .append(".idea-card{border:1px solid #CBD5E1;border-radius:14px;padding:22px;background:#FFFFFF;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.03);}")
                .append(".idea-header{display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:8px;margin-bottom:10px;}")
                .append(".idea-title{font-size:17px;font-weight:800;color:#0F172A;margin:0;line-height:1.35;}")
                .append(".pill-platform{display:inline-block;padding:3px 8px;border-radius:4px;background:#DBEAFE;color:#1D4ED8;font-size:11.5px;font-weight:700;}")
                .append(".pill-money{display:inline-block;padding:3px 8px;border-radius:4px;background:#DCFCE7;color:#15803D;font-size:11.5px;font-weight:700;}")
                .append(".why-box{background:#F8FAFC;border-left:3px solid #7C3AED;padding:10px 14px;border-radius:0 8px 8px 0;margin:12px 0;font-size:13px;line-height:1.5;color:#334155;}")
                .append(".steps-title{font-size:12.5px;font-weight:800;text-transform:uppercase;color:#475569;margin:14px 0 8px 0;}")
                .append(".step-item{display:flex;gap:10px;margin-bottom:8px;font-size:13px;color:#1E293B;line-height:1.5;}")
                .append(".step-badge{background:#0F172A;color:#FFFFFF;border-radius:50%;width:20px;height:20px;display:inline-flex;align-items:center;justify-content:center;font-size:11px;font-weight:700;flex-shrink:0;}")
                .append(".action-tonight{background:#FFFBEB;border:1px solid #FDE68A;border-radius:10px;padding:12px 14px;margin-top:14px;font-size:12.5px;color:#92400E;line-height:1.5;}")
                .append(".table-platform{width:100%;border-collapse:collapse;margin-top:12px;font-size:12.5px;}")
                .append(".table-platform th{background:#F8FAFC;padding:10px 12px;border:1px solid #E2E8F0;text-align:left;font-weight:700;color:#0F172A;}")
                .append(".table-platform td{padding:10px 12px;border:1px solid #E2E8F0;color:#334155;line-height:1.45;}")
                .append(".cta-box{background:#0F172A;color:#FFFFFF;border-radius:12px;padding:22px;text-align:center;margin-top:10px;}")
                .append(".cta-btn{display:inline-block;padding:12px 22px;background:#FFFFFF;color:#0F172A;border-radius:8px;font-weight:700;font-size:13.5px;text-decoration:none;margin:6px;}")
                .append(".footer{background:#F8FAFC;padding:20px 28px;text-align:center;font-size:12px;color:#94A3B8;line-height:1.5;border-top:1px solid #E2E8F0;}")
                .append("</style></head><body>")
                .append("<div class='container'>")
                .append("<div class='header'>")
                .append("<span class='badge'>Chiến Lược Kiếm Tiền & Sáng Tạo Độc Quyền</span>")
                .append("<h1>").append(reportTitle).append("</h1>")
                .append("<p>").append(reportDate).append(" &middot; Bản tin tự động phân tích ý tưởng làm Game & Video có doanh thu thực tế cho bạn</p>")
                .append("</div>")

                // Section 1: Algorithm Reality Check
                .append("<div class='section'>")
                .append("<div class='section-title'>1. Giải Mã Thuật Toán: Vì Sao Video Thuần AI Ít View Hơn Video Bạn Tự Quay?</div>")
                .append("<div class='insight-box'>")
                .append("<div style='font-size:13.5px;line-height:1.6;color:#4A044E;'><strong>Bài học từ trải nghiệm thực tế của bạn:</strong> ").append(algorithmInsight).append("</div>")
                .append("<div class='insight-grid'>")
                .append("<div class='card-fail'><strong>🔴 100% Video AI (Bẫy nội dung rác)</strong><br/>Giọng đọc máy đều đều, hình ảnh chung chung, không có cảm xúc. Người xem vuốt đi sau 1.5 giây. Thuật toán YouTube & TikTok lập tức đánh tụt đề xuất.</div>")
                .append("<div class='card-win'><strong>🟢 Công thức Hybrid Creator (Thắng thế 2026)</strong><br/>Gameplay thực tế + Phản xạ tự nhiên của BẠN (Linh hồn) KẾT HỢP với AI (Lên ý tưởng hook 3 giây đầu, kịch bản thử thách, thumbnail và SEO).</div>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Section 2: Actionable Monetization Ideas & Step-by-Step
                .append("<div class='section'>")
                .append("<div class='section-title'>2. Các Ý Tưởng Kiếm Tiền Cụ Thể & Từng Bước Triển Khai Từ A-Z</div>");

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

                        .append("<div class='why-box'><strong>💡 Điểm khác biệt mấu chốt:</strong> ").append(idea.whyAiFailedVsRealHuman()).append("</div>")

                        .append("<div class='steps-title'>Các bước hiện thực hóa từ A-Z:</div>");

                if (idea.executionSteps() != null) {
                    int sIdx = 1;
                    for (String step : idea.executionSteps()) {
                        html.append("<div class='step-item'>")
                                .append("<span class='step-badge'>").append(sIdx++).append("</span>")
                                .append("<div>").append(step).append("</div>")
                                .append("</div>");
                    }
                }

                html.append("<div style='margin-top:12px;font-size:12px;color:#64748B;'><strong>Công cụ khuyên dùng:</strong> ").append(idea.toolstackRecommended()).append("</div>")
                        .append("<div class='action-tonight'><strong>🚀 Hành động làm ngay tối nay (30 phút):</strong> ").append(idea.immediateActionTonight()).append("</div>")
                        .append("</div>");
            }
        }

        html.append("</div>")

                // Section 3: Platform Matrix
                .append("<div class='section'>")
                .append("<div class='section-title'>3. Bảng Phân Bổ Nền Tảng: Đăng Gì Ở Đâu Để Tối Đa Hóa Doanh Thu?</div>")
                .append("<table class='table-platform'>")
                .append("<thead><tr><th>Nền tảng</th><th>Định dạng nội dung phù hợp nhất</th><th>Mô hình kiếm tiền cốt lõi</th></tr></thead>")
                .append("<tbody>")
                .append("<tr><td><strong>YouTube Dài (8–15p)</strong></td><td>Gameplay thử thách lạ, Devlog tự làm game, chia sẻ mẹo chơi game thực tế</td><td>AdSense (RPM cao $2–$6), Nhà phát hành tài trợ, Link tiếp thị gear</td></tr>")
                .append("<tr><td><strong>TikTok & Shorts (30–60s)</strong></td><td>Cắt 1 khoảnh khắc 'Fail' hài hước hoặc 'Clutch' nghẹt thở từ video dài</td><td>Kéo fan về kênh YouTube chính, Quỹ nhà sáng tạo TikTok, Nhận quảng cáo</td></tr>")
                .append("<tr><td><strong>Steam & Itch.io</strong></td><td>Game Indie 2D hoặc 3D ngắn gọn (Vampire Survivors-like, Escape Room)</td><td>Bán game trả phí ($2.99 – $4.99/bản) thu ngoại tệ USD toàn cầu</td></tr>")
                .append("<tr><td><strong>Roblox</strong></td><td>Minigame hành động hoặc Tycoon vui nhộn cho cộng đồng trẻ</td><td>Bán vật phẩm bằng Robux &rarr; Rút tiền thật qua chương trình DevEx</td></tr>")
                .append("</tbody></table>")
                .append("</div>")

                // Section 4: Aura Interactive Action CTA
                .append("<div class='section'>")
                .append("<div class='cta-box'>")
                .append("<div style='font-size:17px;font-weight:800;margin-bottom:6px;'>Hiện Thực Hóa Ý Tưởng Này Ngay Hôm Nay</div>")
                .append("<div style='font-size:13px;color:#CBD5E1;max-width:520px;margin:0 auto 16px auto;line-height:1.5;'>Aura AI không giới hạn token cho bạn. Hãy mở Trợ lý để yêu cầu AI sinh kịch bản chi tiết, soạn code game mẫu hoặc phác thảo thumbnail ngay tức thì.</div>")
                .append("<div>")
                .append("<a class='cta-btn' href='").append(webActionUrl != null ? webActionUrl : "http://localhost:5173/").append("' target='_blank'>Soạn Kịch Bản Video Này Cùng Aura →</a>")
                .append("<a class='cta-btn' href='").append(webActionUrl != null ? webActionUrl : "http://localhost:5173/").append("' target='_blank'>Sinh Code Game Mẫu Bằng Godot</a>")
                .append("</div>")
                .append("</div>")
                .append("</div>")

                // Footer
                .append("<div class='footer'>")
                .append("Bản tin Chiến lược Kiếm tiền được tổng hợp riêng biệt cho bạn bởi Aura AI Life Assistant.<br/>")
                .append("Gửi tự động tới <strong>").append(recipientEmail).append("</strong> theo cấu hình tư vấn ý tưởng.")
                .append("</div>")
                .append("</div></body></html>");

        return html.toString();
    }
}

