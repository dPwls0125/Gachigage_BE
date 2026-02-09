package com.gachigage.chat.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatMessageType {
	ENTER("입장"), TEXT("텍스트"), IMAGE("이미지"), SYSTEM("시스템");

	private final String name;
}
