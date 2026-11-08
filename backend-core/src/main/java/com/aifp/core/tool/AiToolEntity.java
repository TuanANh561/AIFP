package com.aifp.core.tool;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ai_tools")
public class AiToolEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "permission_level", nullable = false)
    private short permissionLevel;

    @Column(name = "input_schema_json", nullable = false)
    private String inputSchemaJson;

    @Column(name = "timeout_seconds", nullable = false)
    private int timeoutSeconds = 15;

    @Column(name = "rate_limit_per_hour", nullable = false)
    private int rateLimitPerHour = 60;

    @Column(name = "is_enabled_globally", nullable = false)
    private boolean enabledGlobally = true;

    @Column(nullable = false)
    private String version = "1.0.0";

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public short getPermissionLevel() { return permissionLevel; }
    public boolean isEnabledGlobally() { return enabledGlobally; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}

