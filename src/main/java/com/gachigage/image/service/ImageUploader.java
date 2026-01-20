package com.gachigage.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {
	String upload(MultipartFile file);
}
