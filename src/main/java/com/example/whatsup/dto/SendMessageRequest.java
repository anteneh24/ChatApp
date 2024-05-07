
package com.example.whatsup.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
 private String text;
 private Long chatRoomId;
 private String recipientUsername;
 private String attachmentUrl;
}
