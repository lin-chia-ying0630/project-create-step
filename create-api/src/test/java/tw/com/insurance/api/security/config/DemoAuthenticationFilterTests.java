package tw.com.insurance.api.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

class DemoAuthenticationFilterTests {
	@AfterEach
	void 清除測試登入狀態() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void 未登入測試站_建立固定測試身分() throws ServletException, IOException {
		DemoAuthenticationFilter filter = new DemoAuthenticationFilter("NEW_CONTRACT");
		AtomicReference<Authentication> authentication = new AtomicReference<>();

		filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
				(request, response) -> authentication.set(SecurityContextHolder.getContext().getAuthentication()));

		assertThat(authentication.get()).isNotNull();
		assertThat(authentication.get().getPrincipal()).isInstanceOf(Jwt.class);
		Jwt jwt = (Jwt) authentication.get().getPrincipal();
		assertThat(jwt.getSubject()).isEqualTo("demo-user");
		assertThat(jwt.getAudience()).containsExactly("NEW_CONTRACT");
	}
}
