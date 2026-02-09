package com.gachigage.image.service;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("prod")
@Slf4j
public class ObjectStorageImageUploader implements ImageUploader {

	private static final String ENDPOINT = "https://kr.object.ncloudstorage.com";
	private static final String REGION = "kr-standard";
	@Value("${naver.s3.key-id}")
	private String key;
	@Value("${naver.s3.secret-key}")
	private String secret;
	@Value("${naver.s3.bucket-name}")
	private String bucketName;

	@Override
	public String upload(MultipartFile file) {
		try (S3Client s3Client = createClient()) {
			String objectKey = createObjectKey(file.getOriginalFilename());

			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(objectKey)
				.contentType(file.getContentType())
				.acl(software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ)
				.build();

			s3Client.putObject(
				request,
				software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
			);

			log.info("Object Storage 이미지 업로드 성공: {}", ENDPOINT + "/" + bucketName + "/" + objectKey);
			return ENDPOINT + "/" + bucketName + "/" + objectKey;

		} catch (Exception e) {
			throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED, e.getMessage());
		}
	}

	private S3Client createClient() {
		return S3Client.builder()
			.credentialsProvider(
				StaticCredentialsProvider.create(
					AwsBasicCredentials.create(key, secret)))
			.endpointOverride(URI.create(ENDPOINT))
			.region(Region.of(REGION))
			.build();
	}

	private String createObjectKey(String originalFilename) {
		String extension = extractExtension(originalFilename);
		return "images/" + UUID.randomUUID() + extension;
	}

	private String extractExtension(String filename) {
		int index = filename.lastIndexOf(".");
		if (index == -1) {
			throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "파일 확장자가 없습니다.");
		}
		return filename.substring(index);
	}
}
