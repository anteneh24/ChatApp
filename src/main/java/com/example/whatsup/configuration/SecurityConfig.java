
package com.example.whatsup.configuration;

import com.example.whatsup.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

 @Autowired
 private UserDetailsServiceImpl userDetailsService;

 @Autowired
 private JwtAuthFilter jwtAuthFilter;

 @Bean
 public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
  return http
          .cors(AbstractHttpConfigurer::disable)
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authenticationManager(authenticationManager)
          .authorizeHttpRequests(authorizeExchangeSpec ->
                  authorizeExchangeSpec
                          .requestMatchers(HttpMethod.POST,"/api/login/**").permitAll()
                          .requestMatchers(HttpMethod.POST,"/api/signup/**").permitAll()
                          .requestMatchers(HttpMethod.POST,"/swagger-ui/**").permitAll()
                          .requestMatchers(HttpMethod.GET,"/swagger-ui/**").permitAll()
                          .requestMatchers(HttpMethod.POST,"/v3/api-docs/**").permitAll()
                          .requestMatchers(HttpMethod.GET,"/v3/api-docs/**").permitAll()
                          .requestMatchers(HttpMethod.POST,"/swagger-resources/**").permitAll()
                          .requestMatchers(HttpMethod.GET,"/swagger-resources/**").permitAll()
                          .requestMatchers(HttpMethod.POST,"/swagger-resources").permitAll()
                          .requestMatchers(HttpMethod.GET,"/swagger-resources").permitAll()
                          .requestMatchers(HttpMethod.GET,"/swagger-ui.html").permitAll()
                          .anyRequest().authenticated())
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
          .build();
 }


 @Bean
 public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
  AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
  authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  return authenticationManagerBuilder.build();
 }

 @Bean
 public PasswordEncoder passwordEncoder() {
  return new BCryptPasswordEncoder();
 }
}

