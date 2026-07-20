package com.project.linkedin.userService.service;

import com.project.linkedin.userService.dto.LoginRequestDto;
import com.project.linkedin.userService.dto.SignUpRequestDto;
import com.project.linkedin.userService.dto.UserDto;
import com.project.linkedin.userService.entity.User;
import com.project.linkedin.userService.event.UserCreatedEvent;
import com.project.linkedin.userService.exception.BadRequestException;
import com.project.linkedin.userService.repository.UserRepository;
import com.project.linkedin.userService.utils.Bcrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final KafkaTemplate<Long, UserCreatedEvent> userCreatedEventKafkaTemplate;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        log.info("Signing up user with email : {}",signUpRequestDto.getEmail());

        if(userRepository.existsByEmail(signUpRequestDto.getEmail())){
            throw new BadRequestException("User Already exists");
        }

        User user = modelMapper.map(signUpRequestDto, User.class);

        user.setPassword(Bcrypt.hash(signUpRequestDto.getPassword()));

        user = userRepository.save(user);

        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .id(user.getId())
                .name(user.getName())
                .build();

        userCreatedEventKafkaTemplate.send("user_created_topic",userCreatedEvent);

        return modelMapper.map(user, UserDto.class);

    }

    public String login(LoginRequestDto loginRequestDto) {

        log.info("Logging in user with email : {}",loginRequestDto.getEmail());

        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(()-> new BadRequestException("Invalid Email or password"));

        boolean isPasswordMatch = Bcrypt.match(loginRequestDto.getPassword(), user.getPassword());
        if(!isPasswordMatch){
            throw new BadRequestException("Invalid Email or password");
        }

        return jwtService.generateAccessToken(user);

    }
}
