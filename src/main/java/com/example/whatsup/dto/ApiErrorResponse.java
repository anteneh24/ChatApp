
package com.example.whatsup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
 private int errorCode;
 private String description;
}
