package com.example.project3api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roomKey;

    @Column(nullable = false)
    private String externalEntityId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String externalEntityPayload;

    public Long getId() {
        return id;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public String getExternalEntityId() {
        return externalEntityId;
    }

    public String getExternalEntityPayload() {
        return externalEntityPayload;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public void setExternalEntityId(String externalEntityId) {
        this.externalEntityId = externalEntityId;
    }

    public void setExternalEntityPayload(String externalEntityPayload) {
        this.externalEntityPayload = externalEntityPayload;
    }
}
