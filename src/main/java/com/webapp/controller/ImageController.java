package com.webapp.controller;

import com.webapp.exception.FileUploadException;
import com.webapp.exception.FileDeletionException;
import com.webapp.exception.ImageNotFoundException;
import com.webapp.exception.UserNotAuthenticatedException;
import com.webapp.model.Image;
import com.webapp.model.User;
import com.webapp.service.FileStore;
import com.webapp.service.ImageService;
import com.webapp.service.UserService;
import com.webapp.utils.ResponseHandler;
import com.timgroup.statsd.StatsDClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user profile picture upload, retrieval, and deletion.
 */
@RestController
@RequestMapping("/v1/user/self/pic")
@AllArgsConstructor
public class ImageController {

    @Autowired
    private FileStore fileStore;

    @Autowired
    private ImageService imageService;
    @Autowired
    private UserService userService;
    @Autowired
    private StatsDClient statsDClient;

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Operation(summary = "Upload Profile Image", description = "Uploads a profile picture for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or non-image file"),
            @ApiResponse(responseCode = "500", description = "Internal server error while uploading image")
    })
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadFile(@RequestPart("File") MultipartFile file,
                                        @RequestHeader(value = "Authorization") String authorization) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.image.upload.count");

        try {
            User user = userService.getUserByAuthorization(authorization);
            logger.info("{} is uploading a profile image", user.getEmail());

            imageService.saveImage(file, user.getId());
            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.image.upload.duration", duration);

            return ResponseHandler.generateResponse("Image uploaded successfully", HttpStatus.CREATED);

        }
        catch (UserNotAuthenticatedException e) {
            logger.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Unauthorized access", HttpStatus.UNAUTHORIZED);
        }catch (IllegalArgumentException e) {
            logger.warn("Invalid file upload attempt: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Invalid input or non-image file", HttpStatus.BAD_REQUEST);
        } catch (FileUploadException e) {
            logger.error("File upload failed: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Internal server error while uploading image", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error uploading image: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Retrieve Profile Image", description = "Retrieves the profile picture of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No profile image found"),
            @ApiResponse(responseCode = "500", description = "Internal server error while retrieving image")
    })
    @GetMapping
    public ResponseEntity<?> getUserImage(@RequestHeader(value = "Authorization") String authorization) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.image.get.count");

        try {
            User user = userService.getUserByAuthorization(authorization);
            logger.info("{} is retrieving their profile image", user.getEmail());

            Image image = imageService.getImageByUserId(user.getId());
            InputStream imageStream = imageService.getImagefromS3(user.getId());

            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.image.get.duration", duration);

            return ResponseHandler.generateSuccessResponse(new InputStreamResource(imageStream), HttpStatus.OK);

        }
        catch (UserNotAuthenticatedException e) {
            logger.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Unauthorized access", HttpStatus.UNAUTHORIZED);
        }catch (ImageNotFoundException e) {
            logger.warn("Image not found for user: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("No profile image found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Unexpected error retrieving image: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Internal server error while retrieving image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Delete Profile Image", description = "Deletes the profile picture of the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found for deletion"),
            @ApiResponse(responseCode = "500", description = "Internal server error while deleting image")
    })
    @DeleteMapping
    public ResponseEntity<?> deleteUserImage(@RequestHeader(value = "Authorization") String authorization) {
        long startTime = System.currentTimeMillis();
        statsDClient.incrementCounter("api.user.image.delete.count");

        try {
            User user = userService.getUserByAuthorization(authorization);
            logger.info("{} is deleting their profile image", user.getEmail());

            Image image = imageService.getImageByUserId(user.getId());
            imageService.deleteImage(image.getImageId());
            long duration = System.currentTimeMillis() - startTime;
            statsDClient.recordExecutionTime("api.user.image.delete.duration", duration);

            return ResponseHandler.generateResponse("Image deleted ", HttpStatus.OK);

        }
        catch (UserNotAuthenticatedException e) {
            logger.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Unauthorized access", HttpStatus.UNAUTHORIZED);
        }catch (ImageNotFoundException e) {
            logger.warn("Image not found for deletion: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Image not found", HttpStatus.NOT_FOUND);
        } catch (FileDeletionException e) {
            logger.error("File deletion failed: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Internal server error while deleting image", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error deleting image: {}", e.getMessage());
            return ResponseHandler.generateErrorResponse("Failed to delete image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/test-s3-connection")
    public ResponseEntity<String> testS3Connection() {
        boolean isConnected = fileStore.checkS3Connection("1919de32-06d8-089a-f386-848f8fc5e5e9");
        return isConnected ? ResponseEntity.ok("Connected to S3 successfully!")
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to S3");
    }
}
