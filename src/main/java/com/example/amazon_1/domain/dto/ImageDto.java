package com.example.amazon_1.domain.dto;


import com.example.amazon_1.domain.entity.Image;
import lombok.Data;
import lombok.NoArgsConstructor;

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
