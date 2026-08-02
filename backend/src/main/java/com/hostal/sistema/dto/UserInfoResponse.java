package com.hostal.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private String username;
    private String nombreCompleto;
    private String sede;
    private Long sedeId;
}
