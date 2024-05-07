
package com.example.whatsup.service;

import com.example.whatsup.entity.ChatRoom;
import com.example.whatsup.entity.Message;
import com.example.whatsup.entity.User;
import com.example.whatsup.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageService {

 @Autowired
 private MessageRepository messageRepository;

 @Autowired
 private ChatRoomService chatRoomService;

 public Message sendMessage(Message message) {
  return messageRepository.save(message);
 }

 public Page<Message> retrieveMessages(Long chatroomId, Pageable pageable) {
  return messageRepository.findByChatroomIdOrderByCreatedAtDesc(chatroomId, pageable);
 }

 public Message saveMessageWithAttachment(String text, Long chatRoomId, User recipient, String filePath, User sender) {
  ChatRoom chatRoom = chatRoomService.findById(chatRoomId);

  if (chatRoom == null) {
   chatRoom = new ChatRoom();
   chatRoom.setCreatedAt(LocalDateTime.now());
   chatRoom.setName(sender.getUsername()+"_AND_"+recipient.getUsername());
   chatRoom.setUsers(Arrays.asList(sender, recipient));
   chatRoom = chatRoomService.createChatroom(chatRoom);
  }
  Message message = new Message();
  message.setText(text);
  message.setChatroom(chatRoom);
  message.setCreatedAt(LocalDateTime.now());
  message.setAttachmentUrl(filePath);

  return messageRepository.save(message);
 }

 public Optional<Message> getMessageById(Long id) {
  return messageRepository.findById(id);
 }
}

