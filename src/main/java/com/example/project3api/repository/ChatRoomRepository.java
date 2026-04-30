package com.example.project3api.repository;

import com.example.project3api.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByExternalEntityId(String externalEntityId);
}
