package com.korit.security_study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMailReqDto {
    private String email;
}
