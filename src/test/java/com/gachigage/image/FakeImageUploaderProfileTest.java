package com.gachigage.image;

import com.gachigage.image.service.FakeImageUploader;
import com.gachigage.image.service.ImageUploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class FakeImageUploaderProfileTest {

    @Autowired
    private ImageUploader uploader;

    @Test
    @DisplayName("test 프로필을 활성화하면 FakeImageUploader가 활성화된다.")
    void fakeUploaderShouldBeUsedInNonProdProfiles() {
        assertTrue(uploader instanceof FakeImageUploader);
    }
}
