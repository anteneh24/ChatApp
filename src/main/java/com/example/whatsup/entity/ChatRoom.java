
package com.example.whatsup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class ChatRoom {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private String name;
 private LocalDateTime createdAt;

 @JsonIgnore
 @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL)
 @ToString.Exclude
 private List<Message> messages = new ArrayList<>();

 @Transient
 private boolean online;

 @JsonIgnore
 @ManyToMany
 @JoinTable(name = "chat_room_users",
         joinColumns = @JoinColumn(name = "chat_room_id"),
         inverseJoinColumns = @JoinColumn(name = "user_id"))
 @ToString.Exclude
 private List<User> users = new ArrayList<>();

 @Override
 public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
  ChatRoom chatRoom = (ChatRoom) o;
  return getId() != null && Objects.equals(getId(), chatRoom.getId());
 }

 @Override
 public int hashCode() {
  return getClass().hashCode();
 }
}
