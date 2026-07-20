package com.project.linkedin.userService.controller;

import com.project.linkedin.userService.dto.LoginRequestDto;
import com.project.linkedin.userService.dto.SignUpRequestDto;
import com.project.linkedin.userService.dto.UserDto;
import com.project.linkedin.userService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto){

        UserDto userDto = authService.signUp(signUpRequestDto);

        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto){

        String token = authService.login(loginRequestDto);

        return ResponseEntity.ok(token);

    }

}
