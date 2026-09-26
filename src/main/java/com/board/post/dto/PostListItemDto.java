package com.board.post.dto;

import java.time.LocalDateTime;

public record PostListItemDto(
        Integer id,
        String title,
        String authorNickname,
        Long commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
