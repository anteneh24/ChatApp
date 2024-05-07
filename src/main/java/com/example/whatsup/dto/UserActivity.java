
package com.example.whatsup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserActivity {
 private String username;
 private LocalDateTime timestamp;
}
