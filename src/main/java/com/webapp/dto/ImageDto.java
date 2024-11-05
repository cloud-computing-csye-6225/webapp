package com.webapp.dto;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImageDto {
    private long imageId;
    private long userId;
    private String fileName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String s3BucketPath;
}
