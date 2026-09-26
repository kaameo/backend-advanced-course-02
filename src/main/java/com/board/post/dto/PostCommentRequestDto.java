package com.board.post.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCommentRequestDto(
        @NotBlank String content
) {
}
