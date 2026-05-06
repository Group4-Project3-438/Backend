package com.example.project3api.dto;

import com.example.project3api.model.ChatRoom;

public class ChatRoomResponse {
    private Long id;
    private String roomKey;

    public static ChatRoomResponse fromEntity(ChatRoom room) {
        ChatRoomResponse response = new ChatRoomResponse();
        response.setId(room.getId());
        response.setRoomKey(room.getRoomKey());
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }
}
