package com.example.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    //TO-DO: make it safer, require special chars and lower and upper case
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
