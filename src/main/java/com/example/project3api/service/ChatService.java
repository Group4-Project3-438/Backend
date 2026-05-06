package com.example.project3api.service;

import com.example.project3api.dto.ChatMessageResponse;
import com.example.project3api.dto.ChatRoomResponse;
import com.example.project3api.dto.CreateChatMessageRequest;
import com.example.project3api.model.ChatMessage;
import com.example.project3api.model.ChatRoom;
import com.example.project3api.repository.ChatMessageRepository;
import com.example.project3api.repository.ChatRoomRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class ChatService {
    private static final String DEFAULT_ROOM_KEY = "Trades";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(
        ChatRoomRepository chatRoomRepository,
        ChatMessageRepository chatMessageRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public List<ChatRoomResponse> getRooms() {
        ensureDefaultRoomExists();
        return chatRoomRepository.findAllByOrderByRoomKeyAsc()
            .stream()
            .map(ChatRoomResponse::fromEntity)
            .toList();
    }

    @Transactional
    public ChatRoomResponse createRoom(String rawRoomKey) {
        String normalizedRoomKey = normalizeRoomKey(rawRoomKey);
        if (normalizedRoomKey == null) {
            throw new IllegalArgumentException("roomKey is required");
        }
        if (chatRoomRepository.findByRoomKeyIgnoreCase(normalizedRoomKey).isPresent()) {
            throw new IllegalArgumentException("roomKey already exists");
        }

        ChatRoom room = buildRoom(normalizedRoomKey);
        return ChatRoomResponse.fromEntity(chatRoomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long roomId) {
        ensureRoomExists(roomId);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId)
            .stream()
            .map(ChatMessageResponse::fromEntity)
            .toList();
    }

    @Transactional
    public ChatMessageResponse createMessage(
        Long roomId,
        CreateChatMessageRequest request,
        OAuth2User principal,
        OAuth2AuthenticationToken authentication
    ) {
        UserSenderDetails sender = principal != null
            ? resolveSender(principal, authentication)
            : resolveSenderFromRequest(request);
        if (request.getText() == null || request.getText().isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        ChatRoom room = ensureRoomExists(roomId);
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSenderId(sender.senderId());
        message.setSenderName(sender.senderName());
        message.setText(request.getText().trim());
        ChatMessage saved = chatMessageRepository.save(message);
        return ChatMessageResponse.fromEntity(saved);
    }

    private void ensureDefaultRoomExists() {
        if (chatRoomRepository.findByRoomKeyIgnoreCase(DEFAULT_ROOM_KEY).isEmpty()) {
            chatRoomRepository.save(buildRoom(DEFAULT_ROOM_KEY));
        }
    }

    private ChatRoom ensureRoomExists(Long roomId) {
        return chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    private ChatRoom buildRoom(String roomKey) {
        ChatRoom room = new ChatRoom();
        room.setRoomKey(roomKey);
        room.setExternalEntityId(roomKey.toLowerCase(Locale.ROOT));
        room.setExternalEntityPayload(null);
        return room;
    }

    private String normalizeRoomKey(String roomKey) {
        if (!StringUtils.hasText(roomKey)) {
            return null;
        }
        return roomKey.trim();
    }

    private UserSenderDetails resolveSender(OAuth2User principal, OAuth2AuthenticationToken authentication) {
        String provider = authentication != null ? authentication.getAuthorizedClientRegistrationId() : "oauth";
        String email = asTrimmedString(principal.getAttribute("email"));
        String providerId = asTrimmedString(principal.getAttribute("sub"));
        if (providerId == null) {
            providerId = asTrimmedString(principal.getAttribute("id"));
        }

        String senderId = email;
        if (!StringUtils.hasText(senderId) && StringUtils.hasText(provider) && StringUtils.hasText(providerId)) {
            senderId = provider + ":" + providerId;
        }
        if (!StringUtils.hasText(senderId)) {
            throw new IllegalArgumentException("Could not resolve authenticated user id");
        }

        String senderName = asTrimmedString(principal.getAttribute("name"));
        if (!StringUtils.hasText(senderName)) {
            senderName = asTrimmedString(principal.getAttribute("login"));
        }
        if (!StringUtils.hasText(senderName)) {
            senderName = senderId;
        }
        return new UserSenderDetails(senderId, senderName);
    }

    private UserSenderDetails resolveSenderFromRequest(CreateChatMessageRequest request) {
        String senderId = asTrimmedString(request.getSenderId());
        if (!StringUtils.hasText(senderId)) {
            throw new IllegalArgumentException("senderId is required");
        }

        String senderName = asTrimmedString(request.getSenderName());
        if (!StringUtils.hasText(senderName)) {
            senderName = senderId;
        }

        return new UserSenderDetails(senderId, senderName);
    }

    private String asTrimmedString(Object value) {
        if (value == null) {
            return null;
        }
        String converted = value.toString().trim();
        return converted.isEmpty() ? null : converted;
    }

    private record UserSenderDetails(String senderId, String senderName) {
    }
}
