package com.example.amazon_1.controller;

import com.example.amazon_1.service.AwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class IndexRestController {

    private final AwsService awsService;

    /**
     * 파일 업로드
     * @param multipartFiles 저장될 파일
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFile(
            @RequestParam("multipartFiles") List<MultipartFile> multipartFiles
    ){

        return ResponseEntity.ok(awsService.fileUpload(multipartFiles));
    }
}
