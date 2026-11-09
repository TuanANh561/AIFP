package com.aifp.core;

import com.aifp.core.auth.AuthDtos;
import com.aifp.core.auth.AuthService;
import com.aifp.core.auth.UserProfileController;
import com.aifp.core.calendar.CalendarAssistantController;
import com.aifp.core.chat.AiProviderPoolController;
import com.aifp.core.chat.ChatService;
import com.aifp.core.chat.ConversationEntity;
import com.aifp.core.common.AesGcmCryptoService;
import com.aifp.core.common.HealthController;
import com.aifp.core.common.HmacSignatureSigner;
import com.aifp.core.common.IdempotencyUtils;
import com.aifp.core.document.DocumentAssistantController;
import com.aifp.core.memory.AiMemoryEntity;
import com.aifp.core.memory.AiMemoryRepository;
import com.aifp.core.memory.PersonalMemoryService;
import com.aifp.core.notification.EmailCenterService;
import com.aifp.core.notification.EmailLogEntity;
import com.aifp.core.notification.IntegrationEntity;
import com.aifp.core.notification.NotificationCenterService;
import com.aifp.core.scheduler.IdempotentTaskSchedulerService;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.security.EmergencyKillSwitchController;
import com.aifp.core.shopping.AiShoppingController;
import com.aifp.core.task.AiTaskEntity;
import com.aifp.core.task.TaskController;
import com.aifp.core.task.TaskExecutionEntity;
import com.aifp.core.tool.ApprovalController;
import com.aifp.core.tool.ToolPermissionGateService;
import com.aifp.core.tool.ToolRegistryController;
import com.aifp.core.writing.AiWritingController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityAndTenantIsolationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private TaskController taskController;

    @Autowired
    private com.aifp.core.task.AiTaskRepository taskRepository;

    @Autowired
    private ToolPermissionGateService toolPermissionGateService;

    @Autowired
    private EmergencyKillSwitchController killSwitchController;

    @Autowired
    private HmacSignatureSigner hmacSigner;

    @Autowired
    private PersonalMemoryService memoryService;

    @Autowired
    private AiMemoryRepository memoryRepository;

    @Autowired
    private ApprovalController approvalController;

    @Autowired
    private IdempotentTaskSchedulerService schedulerService;

    @Autowired
    private EmailCenterService emailCenterService;

    @Autowired
    private NotificationCenterService notificationCenterService;

    @Autowired
    private AesGcmCryptoService aesGcmCryptoService;

    @Autowired
    private DocumentAssistantController documentAssistantController;

    @Autowired
    private CalendarAssistantController calendarAssistantController;

    @Autowired
    private AiWritingController aiWritingController;

    @Autowired
    private UserProfileController userProfileController;

    @Autowired
    private ToolRegistryController toolRegistryController;

    @Autowired
    private HealthController healthController;

    @Autowired
    private AiShoppingController aiShoppingController;

    @Autowired
    private AiProviderPoolController aiProviderPoolController;

    @Autowired
    private com.aifp.core.vision.AiVisionController aiVisionController;

    @Autowired
    private com.aifp.core.tryon.VirtualTryonController virtualTryonController;

    @Autowired
    private com.aifp.core.pccompanion.PcCompanionController pcCompanionController;

    @Autowired
    private com.aifp.core.notification.LinkValidationService linkValidationService;

    @Test
    @DisplayName("1. Refresh Token Rotation & Reuse Detection revokes compromised token family")
    void testRefreshTokenRotationAndReuseDetection() {
        String email = "user1_" + UUID.randomUUID() + "@aifp.local";
        AuthDtos.AuthTokenResponse initial = authService.register(
                new AuthDtos.RegisterRequest(email, "StrongPass#2026!", "Minh Nguyen")
        );
        assertNotNull(initial.accessToken());
        assertNotNull(initial.refreshToken());

        // First rotation succeeds
        AuthDtos.AuthTokenResponse rotated = authService.rotateRefreshToken(initial.refreshToken());
        assertNotEquals(initial.refreshToken(), rotated.refreshToken());

        // Presenting the old (already used) refresh token triggers Reuse Detection & revokes the family
        ResponseStatusException reuseEx = assertThrows(
                ResponseStatusException.class,
                () -> authService.rotateRefreshToken(initial.refreshToken())
        );
        assertEquals(HttpStatus.UNAUTHORIZED, reuseEx.getStatusCode());

        // Even the newly rotated token in the same compromised family is now revoked
        ResponseStatusException familyRevokedEx = assertThrows(
                ResponseStatusException.class,
                () -> authService.rotateRefreshToken(rotated.refreshToken())
        );
        assertEquals(HttpStatus.UNAUTHORIZED, familyRevokedEx.getStatusCode());
    }

    @Test
    @DisplayName("2. Strict Tenant Isolation prevents User B from reading User A conversations or tasks")
    void testCrossTenantIsolation() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("userA_" + UUID.randomUUID() + "@aifp.local", "PasswordA#2026", "User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("userB_" + UUID.randomUUID() + "@aifp.local", "PasswordB#2026", "User B")
        );

        ConversationEntity convA = chatService.createConversation(userA.userId(), "Kế hoạch tài chính cá nhân");
        chatService.sendMessage(userA.userId(), convA.getId(), new ChatService.SendMessageRequest("Tóm tắt ngân sách tháng 11"));

        // User B attempts to read User A's conversation messages -> 404 Not Found
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> chatService.getConversationMessages(userB.userId(), convA.getId())
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("3. 4-Level AI Tool Permission Gate allows L1, pauses L3 for HITL approval, and blocks L4")
    void testFourLevelToolPermissionGate() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("tooluser_" + UUID.randomUUID() + "@aifp.local", "ToolPass#2026", "Tool Tester")
        );

        // Level 1 (searxng_search) -> ALLOWED immediately
        ToolPermissionGateService.GateDecision l1 = toolPermissionGateService.evaluateToolCall(
                user.userId(), null, "searxng_search", "{\"query\":\"Flutter Intern\"}", "{}"
        );
        assertEquals("ALLOWED", l1.status());
        assertEquals((short) 1, l1.permissionLevel());

        // Level 3 (email_send_external) -> Pauses with WAITING_FOR_APPROVAL & creates approvalRequestId
        ToolPermissionGateService.GateDecision l3 = toolPermissionGateService.evaluateToolCall(
                user.userId(), null, "email_send_external", "{\"to_email\":\"hr@company.com\"}", "{\"node\":\"authorize\"}"
        );
        assertEquals("WAITING_FOR_APPROVAL", l3.status());
        assertEquals((short) 3, l3.permissionLevel());
        assertNotNull(l3.approvalRequestId());

        // Level 4 (shell_exec) -> Blocked with 403 Forbidden
        ResponseStatusException l4Ex = assertThrows(
                ResponseStatusException.class,
                () -> toolPermissionGateService.evaluateToolCall(user.userId(), null, "shell_exec", "{\"command\":\"rm -rf /\"}", "{}")
        );
        assertEquals(HttpStatus.FORBIDDEN, l4Ex.getStatusCode());
    }

    @Test
    @DisplayName("4. Emergency Kill Switch pauses active tasks and increments token_version")
    void testEmergencyKillSwitchAndIdempotency() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("killswitch_" + UUID.randomUUID() + "@aifp.local", "KillPass#2026", "Kill Switch User")
        );
        CustomUserPrincipal principal = new CustomUserPrincipal(user.userId(), user.email(), 1);

        AiTaskEntity task = taskController.createTask(
                principal,
                new TaskController.CreateTaskRequest("Săn việc Java Intern", "Tìm việc mỗi 8h sáng", "DELTA_ONLY")
        ).getBody().data();
        assertEquals("ACTIVE", task.getStatus());

        var summary = killSwitchController.triggerEmergencyStop(principal).getBody().data();
        assertEquals(1, summary.pausedTasksCount());
        assertEquals(2, summary.newTokenVersion());

        AiTaskEntity updatedTask = taskController.getTask(principal, task.getId()).getBody().data();
        assertEquals("PAUSED_BY_KILL_SWITCH", updatedTask.getStatus());

        // Verify deterministic idempotency and content fingerprint calculation
        Instant slot = Instant.parse("2026-11-09T01:00:00Z");
        String key1 = IdempotencyUtils.computeExecutionIdempotencyKey(task.getId(), slot);
        String key2 = IdempotencyUtils.computeExecutionIdempotencyKey(task.getId(), slot);
        assertEquals(key1, key2);

        long ts = Instant.now().getEpochSecond();
        String sig = hmacSigner.signInternalRequest(ts, user.userId(), "{\"prompt\":\"hello\"}");
        assertTrue(hmacSigner.verifyInternalRequest(ts, user.userId(), "{\"prompt\":\"hello\"}", sig));
        assertFalse(hmacSigner.verifyInternalRequest(ts, user.userId(), "{\"prompt\":\"tampered\"}", sig));
    }

    @Test
    @DisplayName("5. Sprint 2: Personal Memory Conflict Superseding & Level 3 HITL Approval Decision")
    void testPersonalMemoryHygieneAndHitlApprovalWorkflow() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("memory_" + UUID.randomUUID() + "@aifp.local", "MemPass#2026", "Memory User")
        );
        CustomUserPrincipal principal = new CustomUserPrincipal(user.userId(), user.email(), 1);

        // Save first ACTIVE memory on topicKey = 'target_role'
        AiMemoryEntity m1 = memoryService.upsertMemory(
                user.userId(), "GOAL", "target_role", "Tìm việc Flutter Intern", false
        );
        assertEquals("ACTIVE", m1.getStatus());

        // Save second ACTIVE memory on same topicKey = 'target_role' -> m1 automatically becomes SUPERSEDED
        AiMemoryEntity m2 = memoryService.upsertMemory(
                user.userId(), "GOAL", "target_role", "Chuyển sang Backend Java Intern", false
        );
        assertEquals("ACTIVE", m2.getStatus());
        assertEquals("SUPERSEDED", memoryRepository.findById(m1.getId()).orElseThrow().getStatus());

        // Level 3 HITL approval request decision flow
        var gate = toolPermissionGateService.evaluateToolCall(
                user.userId(), null, "email_send_external", "{\"to\":\"partner@company.vn\"}", "{}"
        );
        var decided = approvalController.decideApproval(
                principal,
                gate.approvalRequestId(),
                new ApprovalController.ApprovalDecisionRequest("APPROVED")
        ).getBody().data();
        assertEquals("APPROVED", decided.getStatus());
    }

    @Test
    @DisplayName("6. Sprint 3: Idempotent Scheduler, Misfire Window, Delta Fingerprinting & Email Idempotency")
    void testIdempotentSchedulerDeltaFilterAndEmailCenter() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("sched_" + UUID.randomUUID() + "@aifp.local", "SchedPass#2026", "Scheduler User")
        );
        CustomUserPrincipal principal = new CustomUserPrincipal(user.userId(), user.email(), 1);

        AiTaskEntity task = taskController.createTask(
                principal,
                new TaskController.CreateTaskRequest("Tin tuyển dụng sáng 8h", "Tìm việc mỗi sáng", "DELTA_ONLY")
        ).getBody().data();

        Instant slot1 = Instant.parse("2026-11-10T01:00:00Z");
        var items = List.of(
                new IdempotentTaskSchedulerService.DiscoveredItem("Flutter Intern Q1", "https://itviec.com/job-1"),
                new IdempotentTaskSchedulerService.DiscoveredItem("Java Intern Q3", "https://topdev.vn/job-2")
        );

        // Slot 1 execution (within 5 minutes of scheduled slot) -> COMPLETED with 2 new items
        TaskExecutionEntity exec1 = schedulerService.executeScheduledSlot(
                user.userId(), task.getId(), slot1, slot1.plus(5, ChronoUnit.MINUTES), items
        );
        assertEquals("COMPLETED", exec1.getStatus());

        // Re-running exact same slot1 -> Idempotent return of exec1 (no duplicate execution created)
        TaskExecutionEntity exec1Retry = schedulerService.executeScheduledSlot(
                user.userId(), task.getId(), slot1, slot1.plus(6, ChronoUnit.MINUTES), items
        );
        assertEquals(exec1.getId(), exec1Retry.getId());

        // Calling EmailCenterService again for exec1 -> Idempotent return (never double-sends email)
        EmailLogEntity email1 = emailCenterService.sendIdempotentSelfDigest(user.userId(), exec1.getId(), "Subj", "Body");
        EmailLogEntity email2 = emailCenterService.sendIdempotentSelfDigest(user.userId(), exec1.getId(), "Subj", "Body");
        assertEquals(email1.getId(), email2.getId());

        // Slot 2 (Next day) with identical items & DELTA_ONLY -> COMPLETED_NO_DELTA (skips email spam)
        Instant slot2 = Instant.parse("2026-11-11T01:00:00Z");
        TaskExecutionEntity exec2 = schedulerService.executeScheduledSlot(
                user.userId(), task.getId(), slot2, slot2.plus(2, ChronoUnit.MINUTES), items
        );
        assertEquals("COMPLETED_NO_DELTA", exec2.getStatus());

        // Slot 3 delayed by 3 hours (> 2h misfire window) -> SKIPPED_STALE
        Instant slot3 = Instant.parse("2026-11-12T01:00:00Z");
        TaskExecutionEntity exec3 = schedulerService.executeScheduledSlot(
                user.userId(), task.getId(), slot3, slot3.plus(3, ChronoUnit.HOURS), items
        );
        assertEquals("SKIPPED_STALE", exec3.getStatus());
    }

    @Test
    @DisplayName("7. Sprint 4: AES-256-GCM Web Push Credentials, Notification Center & Document/Translation Assistant")
    void testAesGcmWebPushAndDocumentAssistant() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("pwa_" + UUID.randomUUID() + "@aifp.local", "PwaPass#2026", "iPhone PWA User")
        );
        CustomUserPrincipal principal = new CustomUserPrincipal(user.userId(), user.email(), 1);

        // Register iOS 16.4+ Web Push subscription and verify AES-256-GCM encryption round-trip
        IntegrationEntity sub = notificationCenterService.registerWebPushSubscription(
                user.userId(), "https://web.push.apple.com/Q123", "p256dh_key_abc", "auth_secret_xyz"
        );
        String decryptedJson = aesGcmCryptoService.decrypt(sub.getEncryptedCredentials());
        assertTrue(decryptedJson.contains("https://web.push.apple.com/Q123"));

        var notif = notificationCenterService.dispatchNotification(
                user.userId(), null, "TASK_RESULT", "Báo cáo sáng 08:00", "Có 2 tin mới"
        );
        assertTrue(notif.isWebPushSent());

        // Document Q&A & Bilingual Translation
        String sampleDoc = "Giới thiệu chung\n--- Page 2 ---\nChính sách bảo mật HITL Level 3 bắt buộc xác nhận.";
        var qaResp = documentAssistantController.analyzeDocument(
                principal,
                new DocumentAssistantController.DocumentQaRequest("Chinh_Sach.pdf", sampleDoc, "bảo mật HITL ở trang nào?")
        ).getBody().data();
        assertTrue(qaResp.citedPages().contains(2));

        var trResp = documentAssistantController.translateText(
                principal,
                new DocumentAssistantController.TranslateRequest("Sử dụng Spring Boot, LangGraph và pgvector.", "en")
        ).getBody().data();
        assertTrue(trResp.preservedTerms().contains("Spring Boot"));
        assertTrue(trResp.preservedTerms().contains("LangGraph"));
    }

    @Test
    @DisplayName("8. Phase 1 End-to-End Task Parse, Run-Now, Execution History & Toggle Lifecycle")
    void testTaskParseRunNowAndExecutionHistory() {
        AuthDtos.AuthTokenResponse user = authService.register(
                new AuthDtos.RegisterRequest("e2e_" + UUID.randomUUID() + "@aifp.local", "E2ePass#2026", "E2E User")
        );
        CustomUserPrincipal principal = new CustomUserPrincipal(user.userId(), user.email(), 1);

        var parsed = taskController.parseTaskPrompt(
                principal,
                new TaskController.ParseTaskPromptRequest("Tìm tin Flutter Intern mỗi sáng 8h", "Asia/Ho_Chi_Minh")
        ).getBody().data();
        assertEquals("0 0 8 * * *", parsed.cronExpression());
        assertEquals("DELTA_ONLY", parsed.deliveryMode());

        AiTaskEntity created = taskController.createTask(
                principal,
                new TaskController.CreateTaskRequest(parsed.title(), "Tìm tin Flutter Intern mỗi sáng 8h", parsed.deliveryMode())
        ).getBody().data();

        TaskExecutionEntity runNowExec = taskController.runTaskNow(principal, created.getId()).getBody().data();
        assertEquals("COMPLETED", runNowExec.getStatus());

        var history = taskController.listTaskExecutions(principal, created.getId()).getBody().data();
        assertEquals(1, history.size());

        AiTaskEntity toggled = taskController.toggleTaskStatus(principal, created.getId()).getBody().data();
        assertEquals("PAUSED", toggled.getStatus());
    }

    @Test
    @DisplayName("9. P1 Modules: Calendar Assistant Level 3 HITL Approval Gate & AI Writing Studio Isolation")
    void testCalendarAssistantHitlApprovalAndAiWritingStudio() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("calA_" + UUID.randomUUID() + "@aifp.local", "CalPass#2026", "User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("calB_" + UUID.randomUUID() + "@aifp.local", "CalPass#2026", "User B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // Propose calendar event -> pauses at Level 3 HITL gate (PENDING_APPROVAL)
        Instant start = Instant.parse("2026-11-15T02:00:00Z");
        Instant end = start.plus(1, ChronoUnit.HOURS);
        var proposal = calendarAssistantController.proposeCalendarEvent(
                principalA,
                new CalendarAssistantController.CreateCalendarEventRequest(
                        "Phỏng vấn Kỹ sư AI Backend",
                        start,
                        end,
                        "Google Meet",
                        "Trao đổi kiến trúc Spring Boot + LangGraph"
                )
        ).getBody().data();

        assertEquals("WAITING_FOR_APPROVAL", proposal.gateStatus());
        assertEquals((short) 3, proposal.permissionLevel());
        assertEquals("PENDING_APPROVAL", proposal.event().getStatus());
        assertNotNull(proposal.approvalRequestId());

        // User B cannot see User A's calendar events
        assertTrue(calendarAssistantController.listEvents(principalB).getBody().data().isEmpty());

        // User A approves the Level 3 request -> Calendar Event automatically becomes CONFIRMED
        approvalController.decideApproval(
                principalA,
                proposal.approvalRequestId(),
                new ApprovalController.ApprovalDecisionRequest("APPROVED")
        );
        var eventsA = calendarAssistantController.listEvents(principalA).getBody().data();
        assertEquals(1, eventsA.size());
        assertEquals("CONFIRMED", eventsA.get(0).getStatus());

        // AI Writing Studio draft generation & tenant isolation
        var draft = aiWritingController.generateDraft(
                principalA,
                new AiWritingController.CreateDraftRequest(
                        "Báo cáo Sprint 4 AIFP",
                        "REPORT",
                        "EXECUTIVE",
                        "Hoàn thành 100% các module P0 và P1 với cổng bảo mật 4 cấp độ."
                )
        ).getBody().data();
        assertEquals("REPORT", draft.getDocType());
        assertTrue(draft.getContentMarkdown().contains("Báo cáo điều hành"));
        assertEquals(1, aiWritingController.listDrafts(principalA).getBody().data().size());
        assertTrue(aiWritingController.listDrafts(principalB).getBody().data().isEmpty());
    }

    @Test
    @DisplayName("10. Multi-Persona Profile Switcher, 14-Tool Registry & Per-Tenant Security Audit Log Isolation")
    void testMultiPersonaProfileToolRegistryAndSecurityAuditLogs() {
        assertEquals("UP", healthController.checkHealth().getBody().data().get("status"));

        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("personaA_" + UUID.randomUUID() + "@aifp.local", "Persona#2026", "Minh Dev")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("personaB_" + UUID.randomUUID() + "@aifp.local", "Persona#2026", "Lan Student")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        var updatedProfile = userProfileController.updateMyProfile(
                principalA,
                new UserProfileController.UpdateProfileRequest("Minh Senior Dev", "DEVELOPER", "Asia/Ho_Chi_Minh", true)
        ).getBody().data();
        assertEquals("DEVELOPER", updatedProfile.personaType());
        assertTrue(updatedProfile.recommendedTools().contains("rss_fetch"));

        var tools = toolRegistryController.listTools().getBody().data();
        assertEquals(19, tools.size());
        assertEquals((short) 1, tools.get(0).getPermissionLevel());
        assertEquals((short) 4, tools.get(tools.size() - 1).getPermissionLevel());

        var logsA = toolRegistryController.listMyAuditLogs(principalA).getBody().data();
        assertFalse(logsA.isEmpty());
        assertTrue(logsA.stream().allMatch(l -> userA.userId().equals(l.getUserId())));

        var logsB = toolRegistryController.listMyAuditLogs(principalB).getBody().data();
        assertTrue(logsB.stream().allMatch(l -> userB.userId().equals(l.getUserId())));
    }

    @Test
    @DisplayName("11. AI Shopping & Price Tracker (M13): Multi-Store Compare, Target Price Tracking & Tenant Isolation")
    void testAiShoppingAndPriceTrackerModule() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("shopperA_" + UUID.randomUUID() + "@aifp.local", "ShopPass#2026", "Shopper A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("shopperB_" + UUID.randomUUID() + "@aifp.local", "ShopPass#2026", "Shopper B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // 1. Multi-Store Price Comparison (Level 1 tool permission)
        var compareResp = aiShoppingController.compareProducts(
                principalA,
                new AiShoppingController.CompareProductsRequest("Bàn phím cơ không dây", new java.math.BigDecimal("2500000"))
        ).getBody().data();
        assertFalse(compareResp.offers().isEmpty());
        assertTrue(compareResp.bestDealSummary().contains("giá tốt nhất"));

        // 2. Track Price of an Item (Level 2 tool permission)
        var trackedItem = aiShoppingController.trackItem(
                principalA,
                new AiShoppingController.TrackItemRequest(
                        "Bàn phím cơ Keychron K2 Pro",
                        new java.math.BigDecimal("1800000"),
                        new java.math.BigDecimal("2050000"),
                        "VND",
                        "https://shopee.vn/keychron-k2-pro",
                        "Shopee Mall"
                )
        ).getBody().data();
        assertEquals("TRACKING", trackedItem.getStatus());
        assertEquals("VND", trackedItem.getCurrency());

        // 3. User A sees 1 tracked item, User B sees 0 (Tenant Isolation)
        var itemsA = aiShoppingController.listTrackedItems(principalA).getBody().data();
        assertEquals(1, itemsA.size());
        assertEquals(trackedItem.getId(), itemsA.get(0).getId());

        var itemsB = aiShoppingController.listTrackedItems(principalB).getBody().data();
        assertTrue(itemsB.isEmpty());

        // 4. Update status & deletion
        var updatedStatus = aiShoppingController.updateItemStatus(principalA, trackedItem.getId(), "PURCHASED").getBody().data();
        assertEquals("PURCHASED", updatedStatus.getStatus());

        var deleted = aiShoppingController.deleteTrackedItem(principalA, trackedItem.getId()).getBody().data();
        assertTrue(deleted);
        assertTrue(aiShoppingController.listTrackedItems(principalA).getBody().data().isEmpty());
    }

    @Test
    @DisplayName("12. Multi-Provider AI Failover Pool, Encrypted User Keys & Tenant Isolation")
    void testMultiProviderAiPoolAndEncryptedKeyIsolation() {
        // 1. Verify failover pool status endpoint
        var poolResp = aiProviderPoolController.getProviderPool().getBody().data();
        assertEquals(5, poolResp.totalProvidersConfigured());
        assertEquals("GEMINI", poolResp.failoverPool().get(0).providerCode());
        assertEquals("OLLAMA", poolResp.failoverPool().get(4).providerCode());

        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("aipoolA_" + UUID.randomUUID() + "@aifp.local", "PoolPass#2026", "Pool User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("aipoolB_" + UUID.randomUUID() + "@aifp.local", "PoolPass#2026", "Pool User B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // 2. User A registers Gemini and Groq API keys (encrypted with AES-256-GCM)
        aiProviderPoolController.registerUserApiKey(
                principalA,
                new AiProviderPoolController.RegisterApiKeyRequest("GEMINI", "AIzaSyCustomKeyForUserA1234567")
        );
        aiProviderPoolController.registerUserApiKey(
                principalA,
                new AiProviderPoolController.RegisterApiKeyRequest("GROQ", "gsk_groqCustomKeyForUserA890123")
        );

        // 3. User B registers an OpenAI key
        aiProviderPoolController.registerUserApiKey(
                principalB,
                new AiProviderPoolController.RegisterApiKeyRequest("OPENAI", "sk-proj-openaiKeyForUserB999999")
        );

        // 4. Verify tenant isolation & key masking
        var myKeysA = aiProviderPoolController.listMyApiKeys(principalA).getBody().data();
        assertEquals(2, myKeysA.size());
        assertTrue(myKeysA.stream().anyMatch(k -> "GEMINI".equals(k.provider()) && k.maskedKey().startsWith("AIza...")));
        assertTrue(myKeysA.stream().anyMatch(k -> "GROQ".equals(k.provider()) && k.maskedKey().startsWith("gsk_...")));
        assertFalse(myKeysA.stream().anyMatch(k -> "OPENAI".equals(k.provider())));

        var myKeysB = aiProviderPoolController.listMyApiKeys(principalB).getBody().data();
        assertEquals(1, myKeysB.size());
        assertEquals("OPENAI", myKeysB.get(0).provider());
        assertTrue(myKeysB.get(0).maskedKey().startsWith("sk-p..."));
    }

    @Test
    @DisplayName("13. AI Vision & Receipt OCR (M14): Image Analysis, OCR Extraction & Tenant Isolation")
    void testAiVisionAndReceiptOcrModule() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("visionA_" + UUID.randomUUID() + "@aifp.local", "VisionPass#2026", "Vision User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("visionB_" + UUID.randomUUID() + "@aifp.local", "VisionPass#2026", "Vision User B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // 1. User A analyzes a receipt image
        var scannedA = aiVisionController.analyzeImage(
                principalA,
                new com.aifp.core.vision.AiVisionController.VisionAnalyzeRequest(
                        "coffee_receipt.png",
                        "RECEIPT_OCR",
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                        "Trích xuất tên cửa hàng và tổng tiền"
                )
        ).getBody().data();

        assertNotNull(scannedA.getId());
        assertEquals("RECEIPT_OCR", scannedA.getAnalysisMode());
        assertEquals("The Coffee House", scannedA.getMerchantName());
        assertEquals(new java.math.BigDecimal("80000"), scannedA.getTotalAmount());
        assertTrue(scannedA.getExtractedText().contains("Cà phê sữa đá"));

        // 2. User A can list their scans, User B sees 0 scans
        var listA = aiVisionController.listUserRecords(principalA).getBody().data();
        assertEquals(1, listA.size());
        assertEquals(scannedA.getId(), listA.get(0).getId());

        var listB = aiVisionController.listUserRecords(principalB).getBody().data();
        assertTrue(listB.isEmpty());

        // 3. User B cannot delete User A's scan (Tenant isolation check)
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> aiVisionController.deleteRecord(principalB, scannedA.getId())
        );

        // 4. User A deletes their scan
        aiVisionController.deleteRecord(principalA, scannedA.getId());
        assertTrue(aiVisionController.listUserRecords(principalA).getBody().data().isEmpty());
    }

    @Test
    @DisplayName("14. Virtual Try-On (M15): Garment Synthesis, 24h Privacy Auto-Expiry & Tenant Isolation")
    void testVirtualTryonModule() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("tryonA_" + UUID.randomUUID() + "@aifp.local", "TryPass#2026", "Tryon User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("tryonB_" + UUID.randomUUID() + "@aifp.local", "TryPass#2026", "Tryon User B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // 1. User A synthesizes a garment try-on
        var session = virtualTryonController.synthesizeTryon(
                principalA,
                new com.aifp.core.tryon.VirtualTryonController.TryonRequest(
                        "Áo sơ mi linen cổ trụ",
                        "TOP",
                        "https://aifp.local/garment.png",
                        "base64photo..."
                )
        ).getBody().data();

        assertNotNull(session.getId());
        assertEquals("COMPLETED", session.getStatus());
        assertEquals("TOP", session.getGarmentCategory());
        assertTrue(session.getExpiresAt().isAfter(java.time.Instant.now()));

        // 2. User A sees 1 session, User B sees 0 (Tenant Isolation)
        var listA = virtualTryonController.listMySessions(principalA).getBody().data();
        assertEquals(1, listA.size());
        assertEquals(session.getId(), listA.get(0).getId());

        var listB = virtualTryonController.listMySessions(principalB).getBody().data();
        assertTrue(listB.isEmpty());

        // 3. User B cannot delete User A's session
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> virtualTryonController.deleteSession(principalB, session.getId())
        );

        // 4. User A deletes their session (Immediate Privacy Wipe)
        virtualTryonController.deleteSession(principalA, session.getId());
        assertTrue(virtualTryonController.listMySessions(principalA).getBody().data().isEmpty());
    }

    @Test
    @DisplayName("15. PC Companion (M16): Sandboxed Folder Pairing, Directory Traversal Defense & Tenant Isolation")
    void testPcCompanionModule() {
        AuthDtos.AuthTokenResponse userA = authService.register(
                new AuthDtos.RegisterRequest("pca_" + UUID.randomUUID() + "@aifp.local", "PcPass#2026", "PC User A")
        );
        AuthDtos.AuthTokenResponse userB = authService.register(
                new AuthDtos.RegisterRequest("pcb_" + UUID.randomUUID() + "@aifp.local", "PcPass#2026", "PC User B")
        );
        CustomUserPrincipal principalA = new CustomUserPrincipal(userA.userId(), userA.email(), 1);
        CustomUserPrincipal principalB = new CustomUserPrincipal(userB.userId(), userB.email(), 1);

        // 1. Directory Traversal Attack is blocked
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> pcCompanionController.pairDevice(
                        principalA,
                        new com.aifp.core.pccompanion.PcCompanionController.PairDeviceRequest("Hacker PC", "WINDOWS", "C:\\Users\\Admin\\..\\..\\Windows")
                )
        );

        // 2. Valid sandbox folder registers device
        var paired = pcCompanionController.pairDevice(
                principalA,
                new com.aifp.core.pccompanion.PcCompanionController.PairDeviceRequest("MacBook Pro Work", "MACOS", "/Users/developer/AIFP_Workspace")
        ).getBody().data();

        assertNotNull(paired.deviceId());
        assertTrue(paired.pairingToken().startsWith("aifp_pc_"));
        assertEquals("CONNECTED", paired.status());

        // 3. User A sees 1 device, User B sees 0 (Tenant Isolation)
        var devicesA = pcCompanionController.listDevices(principalA).getBody().data();
        assertEquals(1, devicesA.size());
        assertEquals(paired.deviceId(), devicesA.get(0).getId());

        var devicesB = pcCompanionController.listDevices(principalB).getBody().data();
        assertTrue(devicesB.isEmpty());

        // 4. Update status & revoke device
        var paused = pcCompanionController.updateStatus(principalA, paired.deviceId(), "PAUSED").getBody().data();
        assertEquals("PAUSED", paused.getStatus());

        pcCompanionController.revokeDevice(principalA, paired.deviceId());
        assertTrue(pcCompanionController.listDevices(principalA).getBody().data().isEmpty());
    }

    @Test
    @DisplayName("16. Link Validation & Dead Link Prevention Gate: Verify URL liveness and automatic 404 fallback")
    void test16_LinkValidationAndDeadLinkPreventionGate() {
        // 1. Invalid, null, and non-existent URLs return false
        assertFalse(linkValidationService.isUrlLive(null));
        assertFalse(linkValidationService.isUrlLive(""));
        assertFalse(linkValidationService.isUrlLive("   "));
        assertFalse(linkValidationService.isUrlLive("not_a_valid_url_at_all"));
        assertFalse(linkValidationService.isUrlLive("https://this-non-existent-domain-99999999999.org/page"));

        // 2. Localhost internal routes are recognized as valid
        assertTrue(linkValidationService.isUrlLive("http://localhost:5173/"));
        assertTrue(linkValidationService.isUrlLive("/dashboard"));

        // 3. Fallback resolution for dead job links
        String deadJobUrl = "https://gamehub.vn/tuyen-dung-game/"; // 404 URL that previously caused issues
        String resolvedJobUrl = linkValidationService.getVerifiedOrFallbackJobUrl(deadJobUrl, "Tuyển dụng Tester Game Mobile");
        assertNotEquals(deadJobUrl, resolvedJobUrl);
        assertTrue(resolvedJobUrl.contains("itviec.com") || resolvedJobUrl.contains("vieclam24h.vn"));

        // 4. Job list sanitization replaces broken links with verified portals
        EmailCenterService.JobCardItem brokenJob = new EmailCenterService.JobCardItem(
                "Game Hub", "Được tài trợ", "Tester Game", "Cơ hội linh hoạt", "Việt Nam",
                "Part-time", "1.000.000 VNĐ", List.of("Chơi game"), "Mô tả", "Ghi chú",
                deadJobUrl, ""
        );
        EmailCenterService.JobCardItem liveJob = new EmailCenterService.JobCardItem(
                "ITViec", "Cơ hội", "Intern Dev", "Hybrid", "Hà Nội",
                "Full-time", "5.000.000 VNĐ", List.of("Lập trình"), "Mô tả", "Ghi chú",
                "https://itviec.com/it-jobs/intern", ""
        );

        List<EmailCenterService.JobCardItem> sanitizedJobs = linkValidationService.sanitizeJobs(List.of(brokenJob, liveJob));
        assertEquals(2, sanitizedJobs.size());
        assertNotEquals(deadJobUrl, sanitizedJobs.get(0).applyUrl());
        assertEquals("https://itviec.com/it-jobs/intern", sanitizedJobs.get(1).applyUrl());

        // 5. Article list sanitization prunes dead articles
        EmailCenterService.DigestArticleCard deadArticle = new EmailCenterService.DigestArticleCard(
                "Fake News", "fake", "Fake Title", "Summary",
                "https://this-non-existent-domain-99999999999.org/article-404", "", "Hôm nay"
        );
        EmailCenterService.DigestArticleCard liveArticle = new EmailCenterService.DigestArticleCard(
                "VnExpress", "news", "Tin tức", "Summary",
                "https://vnexpress.net/so-hoa", "", "Hôm nay"
        );

        List<EmailCenterService.DigestArticleCard> sanitizedArticles = linkValidationService.sanitizeArticles(List.of(deadArticle, liveArticle));
        assertEquals(1, sanitizedArticles.size());
        assertEquals("https://vnexpress.net/so-hoa", sanitizedArticles.get(0).url());
    }

    @Test
    @DisplayName("17. Google OAuth Login & Multi-Tenant Provisioning: Automatic account creation, token pair and task seeding")
    void test17_GoogleOAuthLoginAndMultiTenantProvisioning() {
        String googleEmail = "google_user_" + UUID.randomUUID() + "@gmail.com";
        String mockToken = "mock_google_" + googleEmail;

        // 1. First login -> Auto-creates account and seeds default tasks
        AuthDtos.AuthTokenResponse res1 = authService.loginWithGoogle(new AuthDtos.GoogleLoginRequest(mockToken));
        assertNotNull(res1);
        assertNotNull(res1.accessToken());
        assertNotNull(res1.refreshToken());
        assertEquals(googleEmail, res1.email());
        assertTrue(res1.displayName().contains("Google User"));

        // 2. Verify tasks seeded for this new user in database
        var tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(res1.userId());
        assertFalse(tasks.isEmpty(), "Default topic tasks must be automatically provisioned for new Google user");
        assertTrue(tasks.size() >= 4, "Should have at least Shopee, Jobs, Tech and AI Evolution tasks");

        // 3. Second login with same Google account -> Logs in existing user with identical UUID
        AuthDtos.AuthTokenResponse res2 = authService.loginWithGoogle(new AuthDtos.GoogleLoginRequest(mockToken));
        assertEquals(res1.userId(), res2.userId(), "Subsequent Google logins must resolve to the same user entity");
        assertEquals(res1.email(), res2.email());
    }
}

