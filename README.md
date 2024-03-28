# amazonPrac

### AmazonS3, RDS, MySql 연동 확인



### S3 설정

- com.example.amazon_1.config.AwsS3Config.java

```java

@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public AmazonS3Client amazonS3Client(){
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
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
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;


    @Transactional
    public List<String> insertFile(List<MultipartFile> multipartFiles){

        List<String> fileURLs = new ArrayList<>();

        multipartFiles.forEach(files -> {

            String fileName = convertFileName(files.getOriginalFilename());
            String URLs = "https://" + bucket + ".s3." +region+".amazonaws.com/" +fileName;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(files.getSize());
            objectMetadata.setContentType(files.getContentType());

            try(InputStream inputStream = files.getInputStream()){
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));
                imageRepository.save(new ImageDto(URLs).toEntity());
                fileURLs.add(URLs);
            }catch (IOException e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패");
            }
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

  

