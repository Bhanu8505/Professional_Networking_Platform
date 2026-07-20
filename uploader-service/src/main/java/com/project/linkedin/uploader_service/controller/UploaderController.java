package com.project.linkedin.uploader_service.controller;

import com.project.linkedin.uploader_service.service.UploaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/file")
public class UploaderController {

    private final UploaderService uploaderService;

    @PostMapping()
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile multipartFile){

        String url = uploaderService.upload(multipartFile);
        return ResponseEntity.ok(url);

    }

}
