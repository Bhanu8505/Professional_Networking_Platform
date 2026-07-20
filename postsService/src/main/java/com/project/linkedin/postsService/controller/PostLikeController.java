package com.project.linkedin.postsService.controller;

import com.project.linkedin.postsService.entity.PostLike;
import com.project.linkedin.postsService.service.PostLikeService;
import com.project.linkedin.postsService.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId){

        postLikeService.likePost(postId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unLikePost(@PathVariable Long postId){

        postLikeService.unLikePost(postId);

        return ResponseEntity.noContent().build();
    }

}
