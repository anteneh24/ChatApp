
package com.example.whatsup.service;

import com.example.whatsup.entity.ChatRoom;
import com.example.whatsup.entity.User;
import com.example.whatsup.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserActivityService userActivityService;

    public ChatRoom findById(Long id) {
        return chatRoomRepository.findById(id).orElse(null);
    }

    public ChatRoom findChatroomByUsers(Long userId1, Long userId2) {
        return chatRoomRepository.findChatroomByUsers(userId1, userId2);
    }

    public Page<ChatRoom> findChatRoomsByUsersContaining(User user, Pageable pageable) {
        Page<ChatRoom> chatRoomsPage = chatRoomRepository.findChatRoomsByUsersContaining(user.getId(), pageable);

        List<ChatRoom> modifiedChatRooms = chatRoomsPage.getContent().stream()
                .peek(chatRoom -> {
                    boolean anyUserOnline = chatRoom.getUsers().stream()
                            .filter(u -> !u.getUsername().equals(user.getUsername()))
                            .anyMatch(u -> userActivityService.getValue(u.getUsername()).isPresent());
                    chatRoom.setOnline(anyUserOnline);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(modifiedChatRooms, pageable, chatRoomsPage.getTotalElements());
    }

    public ChatRoom createChatroom(ChatRoom chatroom) {
        return chatRoomRepository.save(chatroom);
    }
}
