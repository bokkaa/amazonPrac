package com.example.amazon_1.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {



    @Bean
    public AmazonS3Client amazonS3Client(){

        BasicAWSCredentials creds = new BasicAWSCredentials(CloudAws.getAccessKey(), CloudAws.getSecretKey());

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withRegion(CloudAws.getRegion())
                .build();

    }
}
