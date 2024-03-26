# amazonPrac

### AmazonS3, RDS, MySql 연동 확인


### 공통 클래스

<details><summary>com.example.amazon_1.config.AwsProperties.java</summary>

```java

@Component
@ConfigurationProperties(prefix = "cloud.aws")
public class AwsProperties {
    private Credentials credentials;
    private String region;
    private String stackAuto;
    private S3 s3;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStackAuto() {
        return stackAuto;
    }

    public void setStackAuto(String stackAuto) {
        this.stackAuto = stackAuto;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    public static class Credentials {
        private String accessKey;
        private String secretKey;

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }

    public static class S3 {
        private String bucket;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }
}

```
</details> 

### S3 설정

- com.example.amazon_1.config.AwsS3Config.java

```java

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {
    
    private final AwsProperties awsProperties;
    
    @Bean
    public AmazonS3Client amazonS3Client(){

        BasicAWSCredentials creds = new BasicAWSCredentials(awsProperties.getCredentials().getAccessKey(), awsProperties.getCredentials().getSecretKey());

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .withRegion(awsProperties.getRegion())
                .build();
    }
}

```

### 이미지 엔티티 및 Dto 생성

- com.example.amazon_1.domain.entity.Image.java

```java

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileURL;
    @CreatedDate
    private LocalDateTime createdDate;


    @Builder
    public Image(Long id, String fileURL, LocalDateTime createdDate) {
        this.id = id;
        this.fileURL = fileURL;
        this.createdDate = createdDate;
    }
}

```
- com.example.amazon_1.domain.dto.ImageDto.java

```java

@Data
@NoArgsConstructor
public class ImageDto {

    private String fileId;
    private String fileURL;


    public ImageDto(String fileURL) {
        this.fileURL = fileURL;
    }

    public Image toEntity(){
        return Image.builder()
                .fileURL(fileURL)
                .build();
    }

}

```

### 컨트롤러

- com.example.amazon_1.controller.IndexRestController.java

```java

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

```

### 이미지 업로드 및 저장 구현

- com.example.amazon_1.service.AwsService.java

```java

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AwsService {

    public class AwsService {

    private final AwsProperties awsProperties;
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
            String Url = "https://" + awsProperties.getS3().getBucket() + ".s3." + awsProperties.getRegion() + ".amazonaws.com/" + fileName;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(files.getSize());
            objectMetadata.setContentType(files.getContentType());

            try(InputStream inputStream = files.getInputStream()){

                amazonS3.putObject(new PutObjectRequest(awsProperties.getS3().getBucket(), fileName, inputStream, objectMetadata));


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

```

<hr>

### 결과 확인

- 파일 전송
  
![제목 없음](https://github.com/bokkaa/amazonPrac/assets/77730779/f9fd2db7-758d-4fa2-8856-7ed91a0b1042)

- DB 저장
  
![제목 없음](https://github.com/bokkaa/amazonPrac/assets/77730779/c1d3ca27-a649-41c8-9fe0-221535e4ade8)

- S3 저장
- 
![제목 없음](https://github.com/bokkaa/amazonPrac/assets/77730779/a0fee2f6-98f7-4d09-93f8-76539045db62)

  

