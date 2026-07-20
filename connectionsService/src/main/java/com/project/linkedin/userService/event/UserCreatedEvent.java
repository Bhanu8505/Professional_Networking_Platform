package com.project.linkedin.userService.event;

import lombok.Data;

@Data
public class UserCreatedEvent {

    private Long id;
    private String name;

}
