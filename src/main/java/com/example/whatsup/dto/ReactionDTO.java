
package com.example.whatsup.dto;

import com.example.whatsup.entity.EmojiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactionDTO {
 private EmojiType emojiType;
}
