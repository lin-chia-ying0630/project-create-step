package tw.com.insurance.api.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/** 公開測試站未連接公司 IdP 時，提供不含個資且可追蹤的固定測試身分。 */
final class DemoAuthenticationFilter extends OncePerRequestFilter {
	private static final String DEMO_USER_ID = "demo-user";
	private final String audience;

	DemoAuthenticationFilter(String audience) {
		this.audience = audience;
	}

	/**
	 * 僅在目前 request 尚無登入身分時建立短效測試 JWT，讓 Controller 維持單一 principal 契約。
	 *
	 * @param request
	 *            HTTP request
	 * @param response
	 *            HTTP response
	 * @param filterChain
	 *            後續 filter chain
	 * @throws ServletException
	 *             filter chain 執行失敗
	 * @throws IOException
	 *             response I/O 失敗
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			Instant now = Instant.now();
			Jwt jwt = Jwt.withTokenValue("demo-access-token").header("alg", "none").subject(DEMO_USER_ID)
					.audience(List.of(audience)).issuedAt(now).expiresAt(now.plus(1, ChronoUnit.HOURS)).build();
			SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
		}
		filterChain.doFilter(request, response);
	}
}
