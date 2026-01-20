package com.gachigage.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.gachigage.image.service.FakeImageUploader;
import com.gachigage.image.service.ImageUploader;
import com.gachigage.member.MemberRepository;

@SpringBootTest
@Profile("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class FakeImageUploaderProfileTest {
	@Autowired
	private ImageUploader uploader;

	@MockitoBean
	MemberRepository memberRepository;

	@Test
	@DisplayName("test 프로필을 활성화하면 FakeImageUploader가 활성화된다.")
	void fakeUploaderShouldBeUsedInNonProdProfiles() {
		assertTrue(uploader instanceof FakeImageUploader);
	}
}
