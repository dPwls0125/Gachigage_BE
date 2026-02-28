package com.gachigage.global.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.gachigage.global.login.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final AuthenticationSuccessHandler oAuth2SuccessHandler;
	private final CustomOAuth2UserService oAuth2UserService;
	private final JwtProvider jwtProvider;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
		Optional<CorsConfigurationSource> corsConfigurationSource,
		CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {

		if (corsConfigurationSource.isPresent()) {
			http.cors(cors -> cors.configurationSource(corsConfigurationSource.get()));
		} else {
			http.cors(AbstractHttpConfigurer::disable); // prod 등 빈이 없는 경우 CORS 비활성화
		}

		http.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs/**",
					"/stomp/**",
					"/api/stomp/**",
					"/api/**"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/products/**").permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(handler -> handler.authenticationEntryPoint(customAuthenticationEntryPoint))
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(oAuth2UserService))
				.successHandler(oAuth2SuccessHandler))
			.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	@Profile("dev")
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Collections.singletonList("http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Collections.singletonList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
