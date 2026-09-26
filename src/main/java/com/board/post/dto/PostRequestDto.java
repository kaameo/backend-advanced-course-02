package com.board.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequestDto(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content
) {
}
