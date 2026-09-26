package com.board.post.service;

import com.board.global.exception.BadRequestException;
import com.board.global.exception.ForbiddenException;
import com.board.global.exception.NotFoundException;
import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import com.board.post.dto.PostCommentRequestDto;
import com.board.post.dto.PostCommentResponseDto;
import com.board.post.entity.Post;
import com.board.post.entity.PostComment;
import com.board.post.repository.PostCommentRepository;
import com.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PostCommentResponseDto create(int memberId, int postId, PostCommentRequestDto request) {
        Post post = getPost(postId);
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id=" + memberId));
        PostComment saved = postCommentRepository.save(new PostComment(post, author, request.content()));
        return PostCommentResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PostCommentResponseDto> findAllByPost(int postId) {
        getPost(postId);
        List<PostComment> comments = postCommentRepository.findAllWithAuthorByPostId(postId);

        Map<Integer, List<PostCommentResponseDto>> repliesByParentId = comments.stream()
                .filter(PostComment::isReply)
                .collect(Collectors.groupingBy(
                        c -> c.getParentComment().getId(),
                        Collectors.mapping(PostCommentResponseDto::from, Collectors.toList())
                ));

        return comments.stream()
                .filter(c -> !c.isReply())
                .map(c -> PostCommentResponseDto.from(c, repliesByParentId.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional
    public PostCommentResponseDto update(int memberId, int postId, int commentId, PostCommentRequestDto request) {
        PostComment comment = getComment(postId, commentId);
        checkAuthor(comment, memberId);
        comment.update(request.content());
        return PostCommentResponseDto.from(comment);
    }

    @Transactional
    public void delete(int memberId, int postId, int commentId) {
        PostComment comment = getComment(postId, commentId);
        checkAuthor(comment, memberId);
        if (!comment.isReply()) {
            postCommentRepository.deleteAllByParentId(commentId);   // 대댓글 먼저 (외래 키)
        }
        postCommentRepository.delete(comment);
    }

    @Transactional
    public PostCommentResponseDto createReply(int memberId, int postId, int parentId, PostCommentRequestDto request) {
        PostComment parent = getComment(postId, parentId);   // 없거나 다른 글의 댓글이면 404
        if (parent.isReply()) {
            throw new BadRequestException("대댓글에는 답글을 달 수 없습니다.");
        }
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id=" + memberId));
        PostComment saved = postCommentRepository.save(PostComment.reply(parent, author, request.content()));
        return PostCommentResponseDto.from(saved);
    }

    private Post getPost(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 없습니다. id=" + postId));
    }

    private PostComment getComment(int postId, int commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 없습니다. id=" + commentId));
        if (comment.getPost().getId() != postId) {
            throw new NotFoundException("해당 게시글의 댓글이 아닙니다. commentId=" + commentId);
        }
        return comment;
    }

    private void checkAuthor(PostComment comment, int memberId) {
        if (!comment.isAuthor(memberId)) {
            throw new ForbiddenException("본인이 작성한 댓글만 수정·삭제할 수 있습니다.");
        }
    }
}
