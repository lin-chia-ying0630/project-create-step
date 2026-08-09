package tw.com.insurance.api.security;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.com.insurance.api.common.ResponseBodyDto;

/** 回傳已通過後端驗簽的 SSO 身分摘要。 */
@RestController
@RequestMapping("/api/auth")
public class SsoAuthController {
	@GetMapping("/me")
	public ResponseEntity<ResponseBodyDto<Map<String, Object>>> currentUser(@AuthenticationPrincipal Jwt jwt) {
		return ResponseEntity
				.ok(ResponseBodyDto.success("已登入", Map.of("userId", jwt.getSubject(), "audiences", jwt.getAudience())));
	}
}
