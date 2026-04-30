package com.example.project3api.repository;

import com.example.project3api.model.ChatMessage;
import com.example.project3api.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomOrderByCreatedAtAsc(ChatRoom room);
}
