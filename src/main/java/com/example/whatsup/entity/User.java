
package com.example.whatsup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "whats_up_user")
public class User implements UserDetails {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 @Column(unique = true)
 private String username;
 private String password;
 private String profilePictureUrl;
 private String status;

 @ManyToMany(mappedBy = "users")
 @ToString.Exclude
 private List<ChatRoom> chatRooms = new ArrayList<>();
 @Override
 public Collection<? extends GrantedAuthority> getAuthorities() {
  return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
 }

 @Override
 public String getUsername() {
  return username;
 }

 @Override
 public String getPassword() {
  return password;
 }

 @Override
 public boolean isAccountNonExpired() {
  return true;
 }

 @Override
 public boolean isAccountNonLocked() {
  return true;
 }

 @Override
 public boolean isCredentialsNonExpired() {
  return true;
 }

 @Override
 public boolean isEnabled() {
  return true;
 }

 @Override
 public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
  User user = (User) o;
  return getId() != null && Objects.equals(getId(), user.getId());
 }

 @Override
 public int hashCode() {
  return getClass().hashCode();
 }
}