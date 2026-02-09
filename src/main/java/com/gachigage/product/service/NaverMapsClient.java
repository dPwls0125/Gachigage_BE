package com.gachigage.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class NaverMapsClient {
	private final WebClient webClient;

	public NaverMapsClient(WebClient.Builder builder,
		@Value("${naver.maps.base-url}") String baseUrl,
		@Value("${naver.maps.client-id}") String clientId,
		@Value("${naver.maps.client-secret}") String clientSecret
	) {
		this.webClient = builder
			.baseUrl(baseUrl)
			.defaultHeader("x-ncp-apigw-api-key-id", clientId)
			.defaultHeader("x-ncp-apigw-api-key", clientSecret)
			.defaultHeader(HttpHeaders.ACCEPT, "application/json")
			.build();
	}

	public Mono<JsonNode> reverseGeocode(double lon, double lat) {

		log.info("reverseGeocode coords = {}, {}", lon, lat);

		String coords = lon + "," + lat;
		String orders = "legalcode";
		return webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/map-reversegeocode/v2/gc")
				.queryParam("coords", coords)
				.queryParam("output", "json")
				.queryParam("orders", orders)
				.build())
			.retrieve()
			.bodyToMono(JsonNode.class);
	}

}
