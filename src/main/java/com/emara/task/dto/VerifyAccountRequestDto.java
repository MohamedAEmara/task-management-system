package com.emara.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAccountRequestDto {
    private String username;
    private String otp;
}
