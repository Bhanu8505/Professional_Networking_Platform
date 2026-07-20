package com.project.linkedin.notification_service.consumer;

import com.project.linkedin.notification_service.entity.Notification;
import com.project.linkedin.notification_service.service.NotificationService;
import com.project.linkedin.postsService.event.PostCreated;
import com.project.linkedin.postsService.event.PostLiked;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostsConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "post_created_topic")
    public void handlePostCreated(PostCreated postCreated){
        log.info("handle post created : {}",postCreated);

        String message = String.format("Your connection with id %d has created this post %s",postCreated.getOwnerUserId(), postCreated.getContent());

        Notification notification = Notification.builder()
                .userId(postCreated.getUserId())
                .message(message)
                .build();

        notificationService.addNotification(notification);

    }

    @KafkaListener(topics = "post_liked_topic")
    public void handlePostLiked(PostLiked postLiked){
        log.info("handle post liked : {}",postLiked);

        String message = String.format("Your connection with id %d has liked your post", postLiked.getLikedByUserId());

        Notification notification = Notification.builder()
                .userId(postLiked.getOwnerUserId())
                .message(message)
                .build();

        notificationService.addNotification(notification);

    }

}
