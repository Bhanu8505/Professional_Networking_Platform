package com.project.linkedin.postsService.service;

import com.project.linkedin.postsService.auth.AuthContextHolder;
import com.project.linkedin.postsService.client.ConnectionsServiceClient;
import com.project.linkedin.postsService.client.UploaderServiceClient;
import com.project.linkedin.postsService.dto.PersonDto;
import com.project.linkedin.postsService.dto.PostCreateRequestDto;
import com.project.linkedin.postsService.dto.PostDto;
import com.project.linkedin.postsService.entity.Post;
import com.project.linkedin.postsService.event.PostCreated;
import com.project.linkedin.postsService.exception.ResourceNotFoundException;
import com.project.linkedin.postsService.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsServiceClient connectionsServiceClient;
    private final KafkaTemplate<Long, PostCreated> postCreatedKafkaTemplate;
    private final UploaderServiceClient uploaderServiceClient;

    public PostDto createPost(PostCreateRequestDto postCreateRequestDto, MultipartFile multipartFile) {

        Long userId = AuthContextHolder.getCurrentUserId();

        log.info("Creating post for user with id : {} ",userId);

        ResponseEntity<String> imageUrl = uploaderServiceClient.uploadFile(multipartFile);

        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);
        post.setImageUrl(imageUrl.getBody());
        post = postRepository.save(post);

        List<PersonDto> personDtoList = connectionsServiceClient.getFirstDegreeConnections(userId);

        for(PersonDto person: personDtoList){
            PostCreated postCreated = PostCreated.builder()
                    .postId(post.getId())
                    .content(post.getContent())
                    .userId(person.getUserId())
                    .ownerUserId(userId)
                    .build();

            postCreatedKafkaTemplate.send("post_created_topic",postCreated);
        }

        return modelMapper.map(post, PostDto.class);

    }

    public PostDto getPostById(Long postId) {

        log.info("Getting post with id : {} ",postId);

        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post not Found with Id : " + postId));

        return modelMapper.map(post, PostDto.class);

    }

    public List<PostDto> getAllPostsOfUser(Long userId) {

        log.info("Getting  all posts of user with id : {} ",userId);

        List<Post> posts = postRepository.findByUserId(userId);

        return posts
                .stream()
                .map((post)->modelMapper.map(post, PostDto.class))
                .collect(Collectors.toList());

    }

}
