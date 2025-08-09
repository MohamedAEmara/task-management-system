package com.emara.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordDto {
    private String username;
    private String otp;
    private String newPassword;
}
