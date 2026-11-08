package com.aifp.core.chat;

import com.aifp.core.common.ApiResponse;
import com.aifp.core.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationEntity>> createConversation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ChatService.CreateConversationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(chatService.createConversation(principal.userId(), request.title())));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationEntity>>> listConversations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.listConversations(principal.userId())));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageEntity>>> getMessages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID conversationId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.getConversationMessages(principal.userId(), conversationId)
        ));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatService.ChatTurnResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatService.SendMessageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                chatService.sendMessage(principal.userId(), conversationId, request)
        ));
    }
}

