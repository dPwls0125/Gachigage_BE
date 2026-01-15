package com.gachigage.image.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("prod")
public class ObjectStorageImageUploader implements ImageUploader {
    @Override
    public String upload(MultipartFile file) {
        return null; // Todo s3 이미지 업로드 구현
    }
}
