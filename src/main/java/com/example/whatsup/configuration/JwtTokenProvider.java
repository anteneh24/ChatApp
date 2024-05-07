
package com.example.whatsup.configuration;

import com.example.whatsup.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.util.Date;

@Component
public class JwtTokenProvider {

 @Value("${jwt.secret}")
 private String jwtSecret;

 @Value("${jwt.expiration}")
 private int jwtExpiration;

 public String generateToken(Authentication authentication) {
  UserDetails principal = (UserDetails) authentication.getPrincipal();
  Date now = new Date();
  Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);
  return Jwts.builder()
          .subject(principal.getUsername())
          .issuedAt(now)
          .expiration(expiryDate)
          .signWith(SignatureAlgorithm.HS256, jwtSecret)
          .compact();
 }

 public String extractUsername(String token) throws AccessDeniedException {
  return getTokenBody(token).getSubject();
 }

 public Boolean validateToken(String token, UserDetails userDetails) throws AccessDeniedException {
  final String username = extractUsername(token);
  return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
 }

 private Claims getTokenBody(String token) throws AccessDeniedException {
  try {
   return Jwts
           .parser()
           .setSigningKey(jwtSecret)
           .build()
           .parseSignedClaims(token)
           .getPayload();
  } catch (ExpiredJwtException e) {
   throw new AccessDeniedException("Access denied: " + e.getMessage());
  }
 }

 private boolean isTokenExpired(String token) throws AccessDeniedException {
  Claims claims = getTokenBody(token);
  return claims.getExpiration().before(new Date());
 }
}

