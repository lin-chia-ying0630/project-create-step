package tw.com.insurance.api.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import tw.com.insurance.api.common.ResponseBodyDto;

/** 以 SSO RS256 JWT 作為新契約 API 的唯一登入邊界。 */
@Configuration
public class SsoSecurityConfig {
	/** 建立只允許明確 SSO 或 Demo 模式的安全過濾鏈。 */
	@Bean
	SecurityFilterChain ssoSecurityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
			@Value("${sso.mode:sso}") String authenticationMode, @Value("${sso.audience}") String audience)
			throws Exception {
		AuthenticationMode mode = resolveAuthenticationMode(authenticationMode);
		if (mode == AuthenticationMode.DEMO) {
			return http.csrf(csrf -> csrf.disable())
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
					.addFilterBefore(new DemoAuthenticationFilter(audience), AnonymousAuthenticationFilter.class)
					.build();
		}
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/actuator/health/**", "/error").permitAll().requestMatchers("/api/**")
						.authenticated().anyRequest().denyAll())
				.oauth2ResourceServer(oauth -> oauth.bearerTokenResolver(ssoCookieTokenResolver()).jwt(jwt -> {
				}).authenticationEntryPoint((request, response, exception) -> {
					response.setStatus(401);
					response.setContentType("application/json;charset=UTF-8");
					objectMapper.writeValue(response.getWriter(),
							ResponseBodyDto.failure("AUTH-0001", "SSO token 無效或已過期"));
				})).build();
	}

	/** 建立同時驗證 issuer、效期與 audience 的 RS256 JWT decoder。 */
	@Bean
	NimbusJwtDecoder jwtDecoder(@Value("${sso.jwk-set-uri}") String jwkSetUri, @Value("${sso.issuer}") String issuer,
			@Value("${sso.audience}") String audience) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(audience)
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "audience 不符", null));
		decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
		return decoder;
	}

	/** 僅從統一入口設定的 HttpOnly Cookie 解析 SSO access token。 */
	@Bean
	BearerTokenResolver ssoCookieTokenResolver() {
		return request -> request.getCookies() == null
				? null
				: Arrays.stream(request.getCookies()).filter(cookie -> "SSO_ACCESS_TOKEN".equals(cookie.getName()))
						.map(Cookie::getValue).findFirst().orElse(null);
	}

	/**
	 * 解析認證模式並採 fail-closed；公開測試站仍可明確選用 Demo。
	 *
	 * @param configuredMode
	 *            AUTH_MODE 設定值
	 * @return 已驗證的認證模式
	 * @throws IllegalStateException
	 *             設定未知模式時
	 */
	static AuthenticationMode resolveAuthenticationMode(String configuredMode) {
		String normalizedMode = configuredMode == null ? "" : configuredMode.trim().toLowerCase(Locale.ROOT);
		if ("sso".equals(normalizedMode)) {
			return AuthenticationMode.SSO;
		}
		if ("demo".equals(normalizedMode)) {
			return AuthenticationMode.DEMO;
		}
		throw new IllegalStateException("AUTH_MODE 只允許 sso 或 demo");
	}

	enum AuthenticationMode {
		SSO, DEMO
	}
}
