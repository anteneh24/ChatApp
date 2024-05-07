
package com.example.whatsup.configuration;

import com.example.whatsup.dto.ApiErrorResponse;
import com.example.whatsup.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

 private final UserDetailsServiceImpl userDetailsService;
 private final ObjectMapper objectMapper;

 private final JwtTokenProvider jwtTokenProvider;

 public JwtAuthFilter(UserDetailsServiceImpl userDetailsService, ObjectMapper objectMapper, JwtTokenProvider jwtTokenProvider) {
  this.userDetailsService = userDetailsService;
  this.objectMapper = objectMapper;
  this.jwtTokenProvider = jwtTokenProvider;
 }

 @Override
 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException, IOException {
  try {
   String authHeader = request.getHeader("Authorization");
   String token = null;
   String username = null;
   if (authHeader != null && authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7);
    username = jwtTokenProvider.extractUsername(token);
   }
   if (token == null) {
    filterChain.doFilter(request, response);
    return;
   }

   if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    if (jwtTokenProvider.validateToken(token, userDetails)) {
     UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, null);
     authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
     SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
   }

   filterChain.doFilter(request, response);
  } catch (AccessDeniedException e) {
   ApiErrorResponse errorResponse = new ApiErrorResponse(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
   response.setStatus(HttpServletResponse.SC_FORBIDDEN);
   response.getWriter().write(toJson(errorResponse));
  }
 }

 private String toJson(ApiErrorResponse response) {
  try {
   return objectMapper.writeValueAsString(response);
  } catch (Exception e) {
   return "";
  }
 }
}
