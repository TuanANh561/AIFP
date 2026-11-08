package com.aifp.core.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "persona_type", nullable = false)
    private String personaType = "GENERAL";

    @Column(nullable = false)
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(nullable = false)
    private String locale = "vi-VN";

    @Column(name = "notification_email")
    private String notificationEmail;

    @Column(name = "preferred_channels", nullable = false)
    private String preferredChannels = "[\"IN_APP\",\"WEB_PUSH\"]";

    @Column(name = "memory_enabled", nullable = false)
    private boolean memoryEnabled = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UserProfileEntity() {
    }

    public UserProfileEntity(UUID userId, String displayName, String notificationEmail) {
        this.userId = userId;
        this.displayName = displayName;
        this.notificationEmail = notificationEmail;
    }

    public UUID getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPersonaType() { return personaType; }
    public void setPersonaType(String personaType) { this.personaType = personaType; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getLocale() { return locale; }
    public String getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }
    public boolean isMemoryEnabled() { return memoryEnabled; }
    public void setMemoryEnabled(boolean memoryEnabled) { this.memoryEnabled = memoryEnabled; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

