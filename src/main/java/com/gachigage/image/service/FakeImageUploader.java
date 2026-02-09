package com.gachigage.image.service;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("test")
public class FakeImageUploader implements ImageUploader {

	@Override
	public String upload(MultipartFile file) {

		// Simulate upload and return a dummy URL
		String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
		return "https://gachigage-bucket.s3.ap-northeast-2.amazonaws.com/test/" + filename;
	}
}
