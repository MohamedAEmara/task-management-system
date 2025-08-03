package com.emara.task.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAccountRequestDto {
    private String username;
    private String otp;

    @Override
    public String toString() {
        return "username = " + username + "\n" + "otp = " + otp;
    }
}
