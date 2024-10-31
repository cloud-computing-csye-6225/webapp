package com.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.webapp.model.Image;
import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Image findImageByS3BucketPath(String path);

    Image findImageByUserId(long productId);
    Image findImageByImageId(long imageId);
}
