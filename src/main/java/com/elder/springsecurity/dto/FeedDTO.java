package com.elder.springsecurity.dto;

import com.elder.springsecurity.entities.User;

import java.util.List;

public record FeedDTO(
        List<FeedItemDTO> feedItem,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
}
