package com.example.amazon_1.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.amazon_1.config.CloudAws;
import com.example.amazon_1.domain.dto.ImageDto;
import com.example.amazon_1.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AwsService {


    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;


    /**
     * 파일 저장
     * @param multipartFiles 저장될 파일들
     * @return 파일 URL
     */
    @Transactional
    public List<String> fileUpload(List<MultipartFile> multipartFiles){

        List<String> fileURLs = new ArrayList<>();

        multipartFiles.forEach(files -> {

            String fileName = convertFileName(files.getOriginalFilename());
            String Url = "https://" + CloudAws.getBucket() + ".s3." + CloudAws.getRegion() + ".amazonaws.com/" + fileName;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(files.getSize());
            objectMetadata.setContentType(files.getContentType());

            try(InputStream inputStream = files.getInputStream()){

                amazonS3.putObject(new PutObjectRequest(CloudAws.getBucket(), fileName, inputStream, objectMetadata));


            }catch (IOException e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
            }

            fileURLs.add(Url);
            imageRepository.save(new ImageDto(Url).toEntity());

        });

        return fileURLs;
    }

    /**
     * 파일 이름 랜덤 생성
     * @param fileName
     * @return ex) sklds-sdfkj-sdfj.jpg
     */
    private String convertFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }


    /**
     * 파일 확장자 획득
     * @param fileName 파일 이름
     * @return 파일 확장자 ( .jpg, .jpeg ..)
     */
    private String getFileExtension(String fileName){
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        }catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일" + fileName);
        }
    }

}
