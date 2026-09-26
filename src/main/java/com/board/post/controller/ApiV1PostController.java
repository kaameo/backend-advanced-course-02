package com.board.post.controller;

import com.board.post.dto.PostListItemDto;
import com.board.post.dto.PostRequestDto;
import com.board.post.dto.PostResponseDto;
import com.board.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> findById(@PathVariable int id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping("")
    public ResponseEntity<PostResponseDto> create(
            @AuthenticationPrincipal Integer memberId,
            @RequestBody @Valid PostRequestDto postRequestDto
    ) {
        PostResponseDto created = postService.create(memberId, postRequestDto);
        return ResponseEntity
                .created(URI.create("/api/v1/posts/" + created.id()))
                .body(created);

    }

    @GetMapping("")
    public ResponseEntity<Page<PostListItemDto>> findAll(
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> update(@AuthenticationPrincipal Integer memberId,
                                                  @PathVariable int id,
                                                  @RequestBody @Valid PostRequestDto postRequestDto) {
        return ResponseEntity.ok(postService.update(memberId, id, postRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Integer memberId,
                                       @PathVariable int id) {
        postService.delete(memberId, id);
        return ResponseEntity.noContent().build();
    }
}
