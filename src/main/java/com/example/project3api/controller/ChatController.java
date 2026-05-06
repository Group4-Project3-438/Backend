package com.example.project3api.controller;

import com.example.project3api.dto.ChatMessageResponse;
import com.example.project3api.dto.ChatRoomResponse;
import com.example.project3api.dto.CreateChatMessageRequest;
import com.example.project3api.dto.CreateChatRoomRequest;
import com.example.project3api.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/chat", "/chat"})
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms")
    public List<ChatRoomResponse> getRooms() {
        return chatService.getRooms();
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse createRoom(@RequestBody CreateChatRoomRequest request) {
        return chatService.createRoom(request.getRoomKey());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long roomId) {
        return chatService.getMessages(roomId);
    }

    @PostMapping("/rooms/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse createMessage(
        @PathVariable Long roomId,
        @RequestBody CreateChatMessageRequest request,
        @AuthenticationPrincipal OAuth2User principal,
        OAuth2AuthenticationToken authentication
    ) {
        return chatService.createMessage(roomId, request, principal, authentication);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
