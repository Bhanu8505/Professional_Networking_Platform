package com.project.linkedin.postsService.controller;

import com.project.linkedin.postsService.auth.AuthContextHolder;
import com.project.linkedin.postsService.dto.PostCreateRequestDto;
import com.project.linkedin.postsService.dto.PostDto;
import com.project.linkedin.postsService.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPost(@RequestPart("post") PostCreateRequestDto postCreateRequestDto, @RequestPart ("multipartFile") MultipartFile multipartFile){

        PostDto postDto = postService.createPost(postCreateRequestDto, multipartFile);

        return new ResponseEntity<>(postDto , HttpStatus.CREATED);

    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId){

        Long userId = AuthContextHolder.getCurrentUserId();

        PostDto postDto = postService.getPostById(postId);

        return ResponseEntity.ok(postDto);

    }

    @GetMapping("/users/{userId}/allPosts")
    public ResponseEntity<List<PostDto>> getAllPostsOfUser(@PathVariable Long userId){

        List<PostDto> posts = postService.getAllPostsOfUser(userId);

        return ResponseEntity.ok(posts);


    }

}
