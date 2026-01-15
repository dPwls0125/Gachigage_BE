package com.gachigage.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageUploader imageUploader;
    public List<String> uploadImage(List<MultipartFile> files){

        if(files.isEmpty()){
            throw new IllegalArgumentException("이미지 파일이 존재하지 않습니다."); // Todo: CustomeException 변경 사항 반영 된 후 으로 CustomException으로 변경
        }

        return files.stream()
                .map(imageUploader::upload)
                .toList();
    }
}
