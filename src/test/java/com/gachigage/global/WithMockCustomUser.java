package com.gachigage.global;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = MockCustomUserSecurityContextFactory.class) // 아래 Step 2의 클래스를 지정
public @interface WithMockCustomUser {
	long id() default 1L;

	String email() default "test@gmail.com";

	String role() default "USER";

	String name() default "테스터";

	long oauthId() default 111L;
}
