package com.example.project3api.controller;

import com.example.project3api.dto.ChatMessageResponse;
import com.example.project3api.dto.CreateChatMessageRequest;
import com.example.project3api.exception.ExternalApiException;
import com.example.project3api.model.ChatRoom;
import com.example.project3api.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms/{entityId}")
    public ChatRoom getRoom(@PathVariable String entityId) {
        return chatService.ensureRoomByEntityId(entityId);
    }

    @GetMapping("/rooms/{entityId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable String entityId) {
        return chatService.getMessages(entityId);
    }

    @PostMapping("/rooms/{entityId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse createMessage(
        @PathVariable String entityId,
        @RequestBody CreateChatMessageRequest request
    ) {
        return chatService.createMessage(entityId, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Map<String, String>> handleExternalApiError(ExternalApiException ex) {
        int statusCode = ex.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null || status.is1xxInformational()) {
            status = HttpStatus.BAD_GATEWAY;
        }

        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }
}
