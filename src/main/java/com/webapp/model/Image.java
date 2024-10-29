package com.webapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "image_details")
@EntityListeners(AuditingEntityListener.class)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnlyProperty
    @Column(name = "image_id")
    private long imageId;
    @ReadOnlyProperty
    @Column(name = "user_id")
    private long userId;
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "date_created",nullable = false, updatable = false)
    @ReadOnlyProperty
    private LocalDateTime dateCreated;

    private LocalDateTime dateUpdated;
    @ReadOnlyProperty
    @Column(name = "s3_bucket_path")
    private String s3BucketPath;
}
