package com.board.post.service;

import com.board.global.exception.NotFoundException;
import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import com.board.post.dto.PostCommentRequestDto;
import com.board.post.entity.Post;
import com.board.post.entity.PostComment;
import com.board.post.repository.PostCommentRepository;
import com.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public int create(int memberId, int postId, PostCommentRequestDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 없습니다. id=" + postId));
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id=" + memberId));
        PostComment saved = postCommentRepository.save(new PostComment(post, author, request.content()));
        return saved.getId();
    }
}
