package com.example.project3api.service;

import com.example.project3api.dto.CreateChatMessageRequest;
import com.example.project3api.dto.ChatMessageResponse;
import com.example.project3api.model.ChatMessage;
import com.example.project3api.model.ChatRoom;
import com.example.project3api.repository.ChatMessageRepository;
import com.example.project3api.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ExternalEntityService externalEntityService;

    public ChatService(
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository,
        ExternalEntityService externalEntityService
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.externalEntityService = externalEntityService;
    }

    public ChatRoom ensureRoomByEntityId(String entityId) {
        return chatRoomRepository.findByExternalEntityId(entityId)
            .orElseGet(() -> createRoom(entityId));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(String entityId) {
        ChatRoom room = ensureRoomByEntityId(entityId);
        return chatMessageRepository.findByRoomOrderByCreatedAtAsc(room)
            .stream()
            .map(ChatMessageResponse::fromEntity)
            .toList();
    }

    @Transactional
    public ChatMessageResponse createMessage(String entityId, CreateChatMessageRequest request) {
        if (request.getSenderId() == null || request.getSenderId().isBlank()) {
            throw new IllegalArgumentException("senderId is required");
        }
        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        ChatRoom room = ensureRoomByEntityId(entityId);
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSenderId(request.getSenderId());
        message.setSenderName(
            (request.getSenderName() == null || request.getSenderName().isBlank())
                ? "Anonymous"
                : request.getSenderName()
        );
        message.setText(request.getText());
        ChatMessage saved = chatMessageRepository.save(message);
        return ChatMessageResponse.fromEntity(saved);
    }

    private ChatRoom createRoom(String entityId) {
        ChatRoom room = new ChatRoom();
        room.setExternalEntityId(entityId);
        room.setRoomKey("room-" + entityId);
        room.setExternalEntityPayload(externalEntityService.fetchEntityPayload(entityId));
        return chatRoomRepository.save(room);
    }
}
