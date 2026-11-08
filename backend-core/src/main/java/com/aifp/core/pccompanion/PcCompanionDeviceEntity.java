package com.aifp.core.pccompanion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pc_companion_devices")
public class PcCompanionDeviceEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_name", nullable = false, length = 120)
    private String deviceName;

    @Column(name = "os_type", nullable = false, length = 32)
    private String osType; // WINDOWS, MACOS, LINUX

    @Column(name = "pairing_token_hash", nullable = false, length = 64)
    private String pairingTokenHash;

    @Column(name = "sandbox_folder_path", nullable = false, length = 512)
    private String sandboxFolderPath;

    @Column(nullable = false, length = 32)
    private String status = "CONNECTED"; // CONNECTED, PAUSED, REVOKED

    @Column(name = "last_heartbeat_at", nullable = false)
    private Instant lastHeartbeatAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected PcCompanionDeviceEntity() {}

    public PcCompanionDeviceEntity(
            UUID id,
            UUID userId,
            String deviceName,
            String osType,
            String pairingTokenHash,
            String sandboxFolderPath
    ) {
        this.id = id;
        this.userId = userId;
        this.deviceName = deviceName;
        this.osType = osType != null ? osType.toUpperCase() : "WINDOWS";
        this.pairingTokenHash = pairingTokenHash;
        this.sandboxFolderPath = sandboxFolderPath;
        this.status = "CONNECTED";
        this.lastHeartbeatAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getDeviceName() { return deviceName; }
    public String getOsType() { return osType; }
    public String getPairingTokenHash() { return pairingTokenHash; }
    public String getSandboxFolderPath() { return sandboxFolderPath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public void setLastHeartbeatAt(Instant lastHeartbeatAt) { this.lastHeartbeatAt = lastHeartbeatAt; }
    public Instant getCreatedAt() { return createdAt; }
}

