package com.example.amazon_1.config;

import org.springframework.beans.factory.annotation.Value;

public class CloudAws {

    @Value("${cloud.aws.credentials.access-key}")
    private static String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private static String secretKey;

    @Value("${cloud.aws.region.static}")
    private static String region;

    @Value("${cloud.aws.s3.bucket}")
    private static String bucket;


    public static String getAccessKey(){
        return accessKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }

    public static String getRegion() {
        return region;
    }

    public static String getBucket() {
        return bucket;
    }
}
