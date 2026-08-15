package tw.com.insurance.api.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SsoSecurityConfigTests {
	@Test
	void 正式模式_解析為Sso() {
		assertThat(SsoSecurityConfig.resolveAuthenticationMode("sso"))
				.isEqualTo(SsoSecurityConfig.AuthenticationMode.SSO);
	}

	@Test
	void 公開測試站明確啟用Demo_解析為Demo() {
		assertThat(SsoSecurityConfig.resolveAuthenticationMode(" demo "))
				.isEqualTo(SsoSecurityConfig.AuthenticationMode.DEMO);
	}

	@Test
	void 未知認證模式_拒絕啟動而不是降級Demo() {
		assertThatThrownBy(() -> SsoSecurityConfig.resolveAuthenticationMode("typo"))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("只允許 sso 或 demo");
	}
}
