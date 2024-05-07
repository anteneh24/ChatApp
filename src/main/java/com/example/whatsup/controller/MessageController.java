
package com.example.whatsup.controller;

import com.example.whatsup.aspect.TrackUserActivity;
import com.example.whatsup.dto.ReactionDTO;
import com.example.whatsup.dto.SendMessageRequest;
import com.example.whatsup.entity.ChatRoom;
import com.example.whatsup.entity.Message;
import com.example.whatsup.entity.Reaction;
import com.example.whatsup.entity.User;
import com.example.whatsup.service.ChatRoomService;
import com.example.whatsup.service.MessageService;
import com.example.whatsup.service.ReactionService;
import com.example.whatsup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.whatsup.utils.AuthUtils.getAuthenticatedUser;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final List<String> PICTURE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(".mp4", ".avi", ".mov");

    @Value("${upload.path}")
    String uploadPath;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    ReactionService reactionService;

    @TrackUserActivity
    @PostMapping("/send")
    @Transactional
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {
        Optional<User> sender = userService.findByUsername(getAuthenticatedUser().getUsername());
        if (sender.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        Optional<User> recipient = userService.findByUsername(sendMessageRequest.getRecipientUsername());
        if (recipient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Recipient not found");
        }

        ChatRoom chatroom;

        if(sendMessageRequest.getChatRoomId() == null){
            chatroom = new ChatRoom();
            chatroom.setName(sender.get().getUsername()+"_AND_"+recipient.get().getUsername());
            chatroom.setUsers(Arrays.asList(sender.get(), recipient.get()));
            chatroom = chatRoomService.createChatroom(chatroom);
        } else {
            chatroom = chatRoomService.findChatroomByUsers(sender.get().getId(), recipient.get().getId());
        }

        if (chatroom == null) {
            chatroom = new ChatRoom();
            chatroom.setCreatedAt(LocalDateTime.now());
            chatroom.setName(sender.get().getUsername()+"_AND_"+recipient.get().getUsername());
            chatroom.setUsers(Arrays.asList(sender.get(), recipient.get()));
            chatroom = chatRoomService.createChatroom(chatroom);
        }

        Message message = new Message();
        message.setText(sendMessageRequest.getText());
        if (sendMessageRequest.getAttachmentUrl() != null) {
            message.setAttachmentUrl(sendMessageRequest.getAttachmentUrl());
        }
        message.setSender(sender.get());
        message.setChatroom(chatroom);
        message.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageService.sendMessage(message);

        return ResponseEntity.ok(savedMessage);
    }

    @TrackUserActivity
    @GetMapping("/retrieve")
    public ResponseEntity<Page<Message>> retrieveMessages(@RequestParam Long chatroomId, @RequestParam int page, @RequestParam int pageSize) {
        System.out.println("page:" + page);
        System.out.println("page size:"+ pageSize);
        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt").descending());
        System.out.println("pageable page:"+ pageable.getPageNumber());
        System.out.println("pageable page size:"+ pageable.getPageSize());
        Page<Message> messagesPage = messageService.retrieveMessages(chatroomId, pageable);
        return ResponseEntity.ok(messagesPage);
    }

    @TrackUserActivity
    @GetMapping("/chatrooms")
    public ResponseEntity<Page<ChatRoom>> listChatRoomsForUser(@RequestParam int page, @RequestParam int pageSize) {

        Pageable pageable = PageRequest.of(page-1, pageSize, Sort.by("createdAt").descending());

        Optional<User> sender = userService.findByUsername(getAuthenticatedUser().getUsername());

        Page<ChatRoom> chatRooms = chatRoomService.findChatRoomsByUsersContaining(sender.get(),pageable);

        return ResponseEntity.ok(chatRooms);
    }

    @TrackUserActivity
    @PostMapping("/send/attachment")
    public ResponseEntity<String> sendMessageWithAttachment(@RequestParam("text") String text,
                                                            @RequestParam("chatRoomId") String chatRoomId,
                                                            @RequestParam("recipientUsername") String recipientUsername,
                                                            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("File size exceeds the limit");
        }

        Optional<User> sender = userService.findByUsername(getAuthenticatedUser().getUsername());
        if (sender.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String newFilename = sender.get().getUsername() + "_" + timestamp + extension;

        try {
            String directory = getDirectory(extension);
            String filePath = directory + "/" + newFilename;
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            Optional<User> recipient = userService.findByUsername(recipientUsername);
            if (recipient.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

             messageService.saveMessageWithAttachment(text, Long.parseLong(chatRoomId), recipient.get(), filePath, sender.get());

            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    private String getDirectory(String extension) {
        if (PICTURE_EXTENSIONS.contains(extension.toLowerCase())) {

            return uploadPath+"/picture";
        } else if (VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
            return uploadPath+"/video";
        } else {
            return uploadPath+"/other";
        }
    }

    @TrackUserActivity
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<Reaction> addReactionToMessage(@PathVariable Long messageId,
                                                         @RequestBody ReactionDTO reactionDTO) {
        Optional<Message> message = messageService.getMessageById(messageId);
        if (message.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> user = userService.findByUsername(getAuthenticatedUser().getUsername());
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reaction reaction = reactionService.addReaction(reactionDTO.getEmojiType(), message.get(), user.get());
        return ResponseEntity.ok(reaction);
    }
}

