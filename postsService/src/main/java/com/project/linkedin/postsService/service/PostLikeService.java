package com.project.linkedin.postsService.service;

import com.project.linkedin.postsService.auth.AuthContextHolder;
import com.project.linkedin.postsService.entity.Post;
import com.project.linkedin.postsService.entity.PostLike;
import com.project.linkedin.postsService.event.PostLiked;
import com.project.linkedin.postsService.exception.BadRequestException;
import com.project.linkedin.postsService.exception.ResourceNotFoundException;
import com.project.linkedin.postsService.repository.PostLikeRepository;
import com.project.linkedin.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<Long, PostLiked> postLikedKafkaTemplate;

    @Transactional
    public void likePost(Long postId) {

        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("User with ID: {} liking the post with ID: {}", userId, postId);

        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post not Found with Id : " + postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(hasAlreadyLiked){
            throw new BadRequestException("User cannot like a already liked post");
        }

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);

        postLikeRepository.save(postLike);

        PostLiked postLiked =PostLiked.builder()
                .likedByUserId(userId)
                .postId(postId)
                .ownerUserId(post.getUserId())
                .build();

        postLikedKafkaTemplate.send("post_liked_topic",postLiked);



    }

    @Transactional
    public void unLikePost(Long postId) {

        Long userId = 1L;
        log.info("User with ID: {} unliking the post with ID: {}", userId, postId);

        postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post not Found with Id : " + postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!hasAlreadyLiked){
            throw new BadRequestException("User cannot unlike a not liked post");
        }

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);


    }
}
