
package com.example.whatsup.controller;

import com.example.whatsup.configuration.JwtTokenProvider;
import com.example.whatsup.dto.JwtAuthenticationResponse;
import com.example.whatsup.dto.LoginRequest;
import com.example.whatsup.dto.UserSignUpRequest;
import com.example.whatsup.entity.User;
import com.example.whatsup.exception.UsernameAlreadyExistsException;
import com.example.whatsup.service.UserActivityService;
import com.example.whatsup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        userActivityService.trackUser(loginRequest.getUsername());
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignUpRequest userSignUpRequest) {
        try {
            User newUser = new User();
            newUser.setUsername(userSignUpRequest.getUsername());
            if (userSignUpRequest.getProfilePictureUrl() != null) {
                newUser.setProfilePictureUrl(userSignUpRequest.getProfilePictureUrl());
            }

            if (userSignUpRequest.getStatus() != null) {
                newUser.setStatus(userSignUpRequest.getStatus());
            }

            newUser.setPassword(passwordEncoder.encode(userSignUpRequest.getPassword()));

            User savedUser = userService.createUser(newUser);

            return ResponseEntity.ok(savedUser);
        } catch (UsernameAlreadyExistsException ex) {
            return ResponseEntity.badRequest().body("Username already exists: " + userSignUpRequest.getUsername());
        }
    }
}

