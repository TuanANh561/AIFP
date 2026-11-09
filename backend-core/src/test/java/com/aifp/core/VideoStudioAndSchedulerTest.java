package com.aifp.core;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.scheduler.AutonomousSchedulerWorker;
import com.aifp.core.task.AiTaskEntity;
import com.aifp.core.video.VideoStudioController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class VideoStudioAndSchedulerTest {

    @Autowired
    private VideoStudioController videoStudioController;

    @Autowired
    private AutonomousSchedulerWorker schedulerWorker;

    @Test
    @DisplayName("Video Studio Test 1: Plan storyboard generates scenes, camera motions, and audio cues")
    void testPlanStoryboardGeneratesScenes() {
        VideoStudioController.PlanVideoRequest req = new VideoStudioController.PlanVideoRequest(
                "Đêm mưa lạnh. Bóng một người lữ khách đứng yên. Anh vững vàng tiến bước.",
                15.0,
                "SILHOUETTE",
                "DORAEMON",
                "RAIN",
                "Nhân vật áo đen"
        );

        ResponseEntity<ApiResponse<VideoStudioController.StoryboardResponse>> resp = videoStudioController.planVideo(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());

        VideoStudioController.StoryboardResponse plan = resp.getBody().data();
        assertNotNull(plan);
        assertEquals("SILHOUETTE", plan.style());
        assertEquals("DORAEMON", plan.voiceActor());
        assertEquals("RAIN", plan.weatherEffect());
        assertTrue(plan.sceneCount() >= 2);
        assertFalse(plan.scenes().isEmpty());

        for (VideoStudioController.VideoScene scene : plan.scenes()) {
            assertNotNull(scene.narration());
            assertNotNull(scene.cameraMotion());
            assertNotNull(scene.weatherOverlay());
            assertNotNull(scene.audioCue());
        }
    }

    @Test
    @DisplayName("Video Studio Test 2: Level 3 HITL Gate strictly blocks video creation without approval")
    void testCreateVideoRequiresApproval() {
        VideoStudioController.CreateVideoRequest unapprovedReq = new VideoStudioController.CreateVideoRequest(
                "Video Thử Nghiệm",
                "Kịch bản ngắn.",
                15.0,
                "SILHOUETTE",
                "DORAEMON",
                "RAIN",
                "",
                false // NOT approved
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            videoStudioController.createVideo(unapprovedReq);
        });

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Level 3 HITL"));
    }

    @Test
    @DisplayName("Video Studio Test 3: Approved video creation outputs clean 0-watermark MP4 metadata")
    void testCreateVideoApprovedSucceeds() {
        VideoStudioController.CreateVideoRequest approvedReq = new VideoStudioController.CreateVideoRequest(
                "Video Hoàn Hảo",
                "Trong đêm giông bão. Tia chớp rạch ngang trời. Bình minh ló dạng.",
                30.0,
                "SILHOUETTE",
                "DOCUMENTARY",
                "RAIN",
                "",
                true // Approved
        );

        ResponseEntity<ApiResponse<VideoStudioController.VideoCreationResponse>> resp = videoStudioController.createVideo(approvedReq);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());

        VideoStudioController.VideoCreationResponse creation = resp.getBody().data();
        assertNotNull(creation);
        assertEquals("COMPLETED", creation.status());
        assertFalse(creation.hasWatermark()); // STRICT 0 WATERMARK
        assertNotNull(creation.videoUrl());
        assertTrue(creation.videoUrl().endsWith(".mp4"));
        assertEquals("SILHOUETTE", creation.style());
        assertTrue(creation.sceneCount() >= 6 && creation.sceneCount() <= 7);
    }

    @Test
    @DisplayName("Video Studio Test 4: Send video email notification to owner")
    void testSendVideoEmail() {
        VideoStudioController.SendVideoEmailRequest emailReq = new VideoStudioController.SendVideoEmailRequest(
                "Video Triết Lý",
                30.0,
                6,
                "SILHOUETTE",
                "DOCUMENTARY",
                "RAIN",
                "https://example.com/video.mp4",
                "test.recipient@aifp.local"
        );

        ResponseEntity<ApiResponse<String>> resp = videoStudioController.sendVideoEmail(null, emailReq);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().success());
    }

    @Test
    @DisplayName("Scheduler Test 5: Respects per-channel schedule hour slots in Asia/Ho_Chi_Minh")
    void testSchedulerRespectsTimeSlots() {
        AiTaskEntity taskTech = new AiTaskEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Tin tức Công nghệ & AI Mới nhất",
                "Cào tin",
                "DELTA_ONLY"
        );

        AiTaskEntity taskShopee = new AiTaskEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Săn Deal & Khuyến mãi Shopee",
                "Cào deal",
                "DELTA_ONLY"
        );

        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime time07h = ZonedDateTime.of(2026, 11, 9, 7, 0, 0, 0, vnZone);
        ZonedDateTime time11h = ZonedDateTime.of(2026, 11, 9, 11, 0, 0, 0, vnZone);
        ZonedDateTime time14h = ZonedDateTime.of(2026, 11, 9, 14, 0, 0, 0, vnZone);

        // Tech task runs at 7am
        assertTrue(schedulerWorker.isTaskScheduledForSlot(taskTech, time07h));
        assertFalse(schedulerWorker.isTaskScheduledForSlot(taskTech, time14h));

        // Shopee task runs at 11am
        assertTrue(schedulerWorker.isTaskScheduledForSlot(taskShopee, time11h));
        assertFalse(schedulerWorker.isTaskScheduledForSlot(taskShopee, time07h));
    }
}

