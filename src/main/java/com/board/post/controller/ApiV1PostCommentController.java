package com.board.post.controller;

import com.board.post.dto.PostCommentRequestDto;
import com.board.post.dto.PostCommentResponseDto;
import com.board.post.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostCommentService postCommentService;

    @PostMapping("")
    public ResponseEntity<PostCommentResponseDto> create(
            @AuthenticationPrincipal Integer memberId,
            @PathVariable int postId,
            @RequestBody @Valid PostCommentRequestDto postCommentRequestDto
    ) {
        PostCommentResponseDto created = postCommentService.create(memberId, postId, postCommentRequestDto);
        return ResponseEntity
                .created(URI.create("/api/v1/posts/" + postId + "/comments/" + created.id()))
                .body(created);
    }

    @GetMapping("")
    public ResponseEntity<List<PostCommentResponseDto>> findAll(
            @PathVariable int postId
    ) {
        return ResponseEntity.ok(postCommentService.findAllByPost(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<PostCommentResponseDto> update(
            @AuthenticationPrincipal Integer memberId,
            @PathVariable int postId,
            @PathVariable int commentId,
            @RequestBody @Valid PostCommentRequestDto request
    ) {
        return ResponseEntity.ok(postCommentService.update(memberId, postId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Integer memberId,
            @PathVariable int postId,
            @PathVariable int commentId
    ) {
        postCommentService.delete(memberId, postId, commentId);
        return ResponseEntity.noContent().build();
    }


}
