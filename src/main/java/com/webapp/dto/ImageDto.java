package com.webapp.dto;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ImageDto {
    private long imageId;
    private long userId;
    private String fileName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private String s3BucketPath;
}
