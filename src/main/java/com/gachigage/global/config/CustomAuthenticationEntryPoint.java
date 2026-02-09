package com.gachigage.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final HandlerExceptionResolver resolver;

	public CustomAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) {
		log.warn("EntryPoint: method={} requestURI={} contextPath={} servletPath={}",
			request.getMethod(),
			request.getRequestURI(),
			request.getContextPath(),
			request.getServletPath()
		);
		resolver.resolveException(request, response, null, new CustomException(ErrorCode.UNAUTHORIZED));
	}
}
