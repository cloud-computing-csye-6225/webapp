package com.webapp.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Service
public class FileStore {
    @Autowired
    private AmazonS3 amazonS3;

    private static final Logger logger = LogManager.getLogger(FileStore.class);

    public void uploadFile(String path,
                           String fileName,
                           Optional<Map<String, String>> optionalMetaData,
                           InputStream inputStream) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetaData.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });

        try {
             amazonS3.putObject(path, fileName, inputStream, objectMetadata);
            logger.info("File successfully uploaded to S3 at path: {}/{}", path, fileName);
        } catch (AmazonS3Exception e) {
            // Amazon S3 couldn't process the request, e.g., bucket doesn't exist or access denied
            logger.error("AmazonS3Exception: Failed to upload file to S3. Error Message: {}, Status Code: {}, AWS Error Code: {}, Error Type: {}, Request ID: {}",
                    e.getMessage(), e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
            throw new IllegalStateException("Failed to upload file due to S3 error", e);
        } catch (AmazonServiceException e) {
            // AWS Service exception, error occurred on the AWS side (service unavailable, etc.)
            logger.error("AmazonServiceException: Request made it to AWS, but was rejected. Error Message: {}, Status Code: {}, AWS Error Code: {}, Error Type: {}, Request ID: {}",
                    e.getMessage(), e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
            throw new IllegalStateException("Failed to upload file due to AWS service error", e);
        } catch (SdkClientException e) {
            // Client-side error, such as network issues or invalid AWS credentials
            logger.error("SdkClientException: Client error occurred when trying to communicate with S3. Error Message: {}",
                    e.getMessage());
            throw new IllegalStateException("Failed to upload file due to client error", e);
        } catch (Exception e) {
            // General catch-all for any other exceptions
            logger.error("Exception: An unexpected error occurred during file upload. Error Message: {}", e.getMessage());
            throw new IllegalStateException("Failed to upload file due to an unexpected error", e);
        }
    }
    public void deleteFile(String path,String fileName) {
        try {
            amazonS3.deleteObject(path,fileName);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to delete the file", e);
        }
    }

    public boolean checkS3Connection(String bucketName) {
        try {
            // Attempt to list objects in the bucket as a simple connectivity test
            amazonS3.listObjectsV2(bucketName);
            logger.info("Successfully connected to S3 bucket: {}", bucketName);
            return true;
        } catch (AmazonServiceException e) {
            logger.error("Failed to connect to S3 bucket: {}", bucketName, e);
            return false;
        }
    }

    public InputStream retrieveFile(String path, String fileName) {
        try {
            S3Object s3Object = amazonS3.getObject(path, fileName);
            logger.info("File successfully retrieved from S3 at path: {}", path);
            return s3Object.getObjectContent();
        } catch (AmazonS3Exception e) {
            logger.error("Failed to retrieve file from S3. Error: {}", e.getMessage());
            throw new IllegalStateException("Failed to retrieve file due to S3 error", e);
        }
    }


}

