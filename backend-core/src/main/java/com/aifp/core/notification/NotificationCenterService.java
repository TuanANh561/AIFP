package com.aifp.core.notification;

import com.aifp.core.common.AesGcmCryptoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationCenterService {

    private final NotificationRepository notificationRepository;
    private final IntegrationRepository integrationRepository;
    private final AesGcmCryptoService cryptoService;

    public NotificationCenterService(
            NotificationRepository notificationRepository,
            IntegrationRepository integrationRepository,
            AesGcmCryptoService cryptoService
    ) {
        this.notificationRepository = notificationRepository;
        this.integrationRepository = integrationRepository;
        this.cryptoService = cryptoService;
    }

    @Transactional
    public IntegrationEntity registerWebPushSubscription(UUID userId, String endpoint, String p256dhKey, String authSecret) {
        String subscriptionPayloadJson = "{\"endpoint\":\"" + endpoint + "\",\"p256dh\":\"" + p256dhKey + "\",\"auth\":\"" + authSecret + "\"}";
        byte[] cipherBytes = cryptoService.encrypt(subscriptionPayloadJson);
        IntegrationEntity integration = new IntegrationEntity(
                UUID.randomUUID(),
                userId,
                "WEB_PUSH_SUBSCRIPTION",
                cipherBytes
        );
        return integrationRepository.save(integration);
    }

    @Transactional
    public NotificationEntity dispatchNotification(UUID userId, UUID executionId, String type, String title, String body) {
        List<IntegrationEntity> activePushSubs = integrationRepository.findByUserIdAndProviderAndStatus(
                userId, "WEB_PUSH_SUBSCRIPTION", "ACTIVE"
        );
        boolean pushSent = !activePushSubs.isEmpty();
        NotificationEntity entity = new NotificationEntity(
                UUID.randomUUID(),
                userId,
                executionId,
                type,
                title,
                body,
                pushSent
        );
        return notificationRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<NotificationEntity> listNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public NotificationEntity markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity entity = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        entity.setRead(true);
        return notificationRepository.save(entity);
    }
}

