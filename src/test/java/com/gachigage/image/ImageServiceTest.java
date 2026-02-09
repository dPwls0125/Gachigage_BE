package com.gachigage.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.gachigage.image.service.ImageService;
import com.gachigage.image.service.ImageUploader;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@Mock
	ImageUploader imageUploader;

	@InjectMocks
	ImageService imageService;

	@Test
	@DisplayName("빈 리스트가 들어오면, IllegaArgumentException을 던진다.")
	void shouldThrowExceptionWhenFilesIsEmpty() {
		// given
		List<MultipartFile> emptyFiles = List.of();

		// when & then
		assertThrows(IllegalArgumentException.class,
			() -> imageService.uploadImage(emptyFiles));
	}

	@Test
	@DisplayName("각 이미지 파일에 대해 imageUploaderFile 한번씩 호출된다.")
	void shouldCallUploaderForEachFileAndReturnUrls() {
		// given
		MultipartFile f1 = new MockMultipartFile("file", "a.jpg", "image/jpeg", new byte[] {});
		MultipartFile f2 = new MockMultipartFile("file", "b.jpg", "image/jpeg", new byte[] {});

		when(imageUploader.upload(f1)).thenReturn("url1");
		when(imageUploader.upload(f2)).thenReturn("url2");

		// when
		List<String> result = imageService.uploadImage(List.of(f1, f2));

		// then
		assertEquals(2, result.size());
		assertEquals("url1", result.get(0));
		assertEquals("url2", result.get(1));

		verify(imageUploader, times(1)).upload(f1);
		verify(imageUploader, times(1)).upload(f2);
	}
}
