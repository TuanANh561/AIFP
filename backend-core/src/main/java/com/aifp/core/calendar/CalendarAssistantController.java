package com.aifp.core.calendar;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import com.aifp.core.tool.ToolPermissionGateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarAssistantController {

    private final CalendarEventRepository calendarRepository;
    private final ToolPermissionGateService toolPermissionGateService;

    public CalendarAssistantController(
            CalendarEventRepository calendarRepository,
            ToolPermissionGateService toolPermissionGateService
    ) {
        this.calendarRepository = calendarRepository;
        this.toolPermissionGateService = toolPermissionGateService;
    }

    public record CreateCalendarEventRequest(
            @NotBlank String title,
            @NotNull Instant startTime,
            @NotNull Instant endTime,
            String location,
            String description
    ) {}

    public record CalendarEventProposalResponse(
            CalendarEventEntity event,
            String gateStatus,
            short permissionLevel,
            UUID approvalRequestId
    ) {}

    @GetMapping("/events")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<CalendarEventEntity>>> listEvents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                calendarRepository.findByUserIdOrderByStartTimeAsc(principal.userId())
        ));
    }

    @PostMapping("/events")
    @Transactional
    public ResponseEntity<ApiResponse<CalendarEventProposalResponse>> proposeCalendarEvent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateCalendarEventRequest request
    ) {
        String safeTitle = request.title().trim().replace("\"", "\\\"");
        String safeLoc = request.location() != null ? request.location().trim().replace("\"", "\\\"") : "";
        String payloadJson = "{\"title\":\"" + safeTitle + "\",\"startTime\":\"" + request.startTime()
                + "\",\"endTime\":\"" + request.endTime() + "\",\"location\":\"" + safeLoc + "\"}";

        ToolPermissionGateService.GateDecision gate = toolPermissionGateService.evaluateToolCall(
                principal.userId(),
                null,
                "calendar_event_write",
                payloadJson,
                "{\"module\":\"CALENDAR_ASSISTANT\",\"action\":\"CREATE_EVENT\"}"
        );

        String eventStatus = "WAITING_FOR_APPROVAL".equals(gate.status()) ? "PENDING_APPROVAL" : "CONFIRMED";
        CalendarEventEntity entity = new CalendarEventEntity(
                UUID.randomUUID(),
                principal.userId(),
                request.title().trim(),
                request.startTime(),
                request.endTime(),
                request.location() != null ? request.location().trim() : null,
                request.description() != null ? request.description().trim() : null,
                eventStatus,
                gate.approvalRequestId()
        );
        CalendarEventEntity saved = calendarRepository.save(entity);

        return ResponseEntity.ok(ApiResponse.ok(new CalendarEventProposalResponse(
                saved,
                gate.status(),
                gate.permissionLevel(),
                gate.approvalRequestId()
        )));
    }
}

