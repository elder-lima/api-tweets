package com.elder.springsecurity.dto;

public record LoginResponseDTO(

        String accessToken,
        Long expiresIn

) {}
