package com.board.post.service;

import com.board.global.exception.ForbiddenException;
import com.board.global.exception.NotFoundException;
import com.board.member.entity.Member;
import com.board.member.repository.MemberRepository;
import com.board.post.dto.PostListItemDto;
import com.board.post.dto.PostRequestDto;
import com.board.post.dto.PostResponseDto;
import com.board.post.entity.Post;
import com.board.post.repository.PostCommentRepository;
import com.board.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public PostResponseDto create(int memberId, PostRequestDto postRequestDto) {
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다. id: " + memberId));

        Post saved = postRepository.save(new Post(postRequestDto.title(), postRequestDto.content(), author));

        return PostResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public PostResponseDto findById(int postId) {
        Post post = getPost(postId);
        return PostResponseDto.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostListItemDto> findAll(Pageable pageable) {
        return postRepository.findPostList(pageable);
    }


    @Transactional
    public PostResponseDto update(int memberId, int postId, PostRequestDto request) {
        Post post = getPost(postId);
        checkAuthor(post, memberId);
        post.update(request.title(), request.content());
        return PostResponseDto.from(post);
    }

    @Transactional
    public void delete(int memberId, int postId) {
        Post post = getPost(postId);
        checkAuthor(post, memberId);
        postCommentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    private Post getPost(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글이 없습니다. id=" + postId));
    }

    private void checkAuthor(Post post, int memberId) {
        if (!post.isAuthor(memberId)) {
            throw new ForbiddenException("본인이 작성한 글만 수정 및 삭제할 수 있습니다.");
        }
    }
}
