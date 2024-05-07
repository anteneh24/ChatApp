
package com.example.whatsup.dto;

import lombok.Data;

@Data
public class UserSignUpRequest {
    private String username;
    private String password;
    private String profilePictureUrl;
    private String status;
}
