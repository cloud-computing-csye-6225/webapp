package com.webapp.config;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonConfig {

    @Value("${amazonProperties.clientRegion}")
    private String clientRegion;

    @Bean
    public AmazonS3 s3() {

        return AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new ProfileCredentialsProvider("dev"))
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
    }

    @Bean
    public AmazonSNSClient snsClient() {
        return (AmazonSNSClient) AmazonSNSClient.builder()
                .withRegion(clientRegion)
                .withCredentials(new ProfileCredentialsProvider("dev"))
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .build();
    }
}
