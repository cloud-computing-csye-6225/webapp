package com.webapp.service;

import com.amazonaws.AmazonServiceException;
import com.webapp.exception.FileUploadException;
import com.webapp.exception.FileDeletionException;
import com.webapp.exception.ImageNotFoundException;
import com.webapp.dto.ImageDto;
import com.webapp.model.Image;
import com.webapp.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

/**
 * Service for handling user profile picture upload and deletion.
 */
@Service
public class ImageService {

    @Autowired
    private FileStore fileStore;
    @Autowired
    private ImageRepository imageRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${S3_BUCKET}")
    private String bucketName;

    public void saveImage(MultipartFile file, Long userId) {
        // Check if the file is empty
        if (file.isEmpty()) {
            logger.warn("No file to upload for user {}", userId);
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }

        // Check if the file is an image
        if (!Arrays.asList(IMAGE_PNG.getMimeType(), IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
            logger.warn("Invalid file type uploaded by user {}", userId);
            throw new IllegalArgumentException("File uploaded is not a valid image.");
        }

        // If an image already exists for the user, delete it before uploading the new one
        Image existingImage = imageRepository.findImageByUserId(userId);
        if (existingImage != null) {
            try {
                logger.info("Deleting existing image for user {}", userId);
                fileStore.deleteFile(existingImage.getS3BucketPath(), existingImage.getFileName());
                imageRepository.delete(existingImage);
            } catch (AmazonServiceException e) {
                logger.error("Failed to delete existing image for user {}: {}", userId, e.getErrorMessage());
                throw new FileDeletionException("Failed to delete existing image for user " + userId, e);
            }
        }

        // Prepare metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));

        // Define file path and name
        Long fileId = generateRandomId();
        String path = String.format("%s/%s/%s", bucketName, userId.toString(), fileId.toString());
        String fileName = file.getOriginalFilename();

        try {
            logger.info("Uploading new image for user {} to path {}", userId, path);
            fileStore.uploadFile(path, fileName, Optional.of(metadata), file.getInputStream());
        } catch (IOException e) {
            logger.error("I/O error uploading image for user {}: {}", userId, e.getMessage());
            throw new FileUploadException("Unable to upload file due to I/O error", e);
        } catch (AmazonServiceException e) {
            logger.error("AWS error uploading image for user {}: {}", userId, e.getErrorMessage());
            throw new FileUploadException("AWS service failed to upload file", e);
        }

        // Save image details in database
        Image image = Image.builder()
                .userId(userId)
                .s3BucketPath(path)
                .fileName(fileName)
                .build();
        imageRepository.save(image);
        logger.info("Image successfully uploaded and saved for user {}", userId);
    }

    public Image getImageByUserId(Long userId) {
        Image image = imageRepository.findImageByUserId(userId);
        if (image == null) {
            logger.warn("No image found for user {}", userId);
            throw new ImageNotFoundException("Image not found for user " + userId);
        }
        return image;
    }

    private long generateRandomId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    public void deleteImage(long imageId) {
        Image image = imageRepository.findImageByImageId(imageId);

        if (image == null) {
            logger.warn("No image with ID {} found for deletion", imageId);
            throw new ImageNotFoundException("Image not found with ID " + imageId);
        }

        try {
            logger.info("Deleting image with ID {}", imageId);
            fileStore.deleteFile(image.getS3BucketPath(), image.getFileName());
            imageRepository.delete(image);
        } catch (AmazonServiceException e) {
            logger.error("Failed to delete image with ID {}: {}", imageId, e.getErrorMessage());
            throw new FileDeletionException("Unable to delete file from S3", e);
        }
    }

    public ImageDto convertToDto(Image image) {
        ImageDto imageDto = new ImageDto();
        imageDto.setImageId(image.getImageId());
        imageDto.setUserId(image.getUserId());
        imageDto.setFileName(image.getFileName());
        imageDto.setDateCreated(image.getDateCreated());
        imageDto.setDateUpdated(image.getDateUpdated());
        imageDto.setS3BucketPath(image.getS3BucketPath());
        return imageDto;
    }

    public Image convertToEntity(ImageDto imageDto) {
        return Image.builder()
                .imageId(imageDto.getImageId())
                .userId(imageDto.getUserId())
                .fileName(imageDto.getFileName())
                .dateCreated(imageDto.getDateCreated())
                .dateUpdated(imageDto.getDateUpdated())
                .s3BucketPath(imageDto.getS3BucketPath())
                .build();
    }
}
