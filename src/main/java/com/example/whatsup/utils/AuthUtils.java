
package com.example.whatsup.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class AuthUtils {

 public static com.example.whatsup.entity.User getAuthenticatedUser() {
  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  if (authentication != null && authentication.getPrincipal() instanceof User) {
   com.example.whatsup.entity.User user = new com.example.whatsup.entity.User();
   user.setUsername(((User) authentication.getPrincipal()).getUsername());
   user.setPassword(((User) authentication.getPrincipal()).getPassword());
   return user;
  }
  return null;
 }
}
