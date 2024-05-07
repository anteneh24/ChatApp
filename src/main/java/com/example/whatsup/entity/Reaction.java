
package com.example.whatsup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Reaction {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Enumerated(EnumType.STRING)
 private EmojiType emojiType;

 @JsonIgnore
 @ManyToOne
 private Message message;

 @JsonIgnore
 @ManyToOne
 private User user;

}

