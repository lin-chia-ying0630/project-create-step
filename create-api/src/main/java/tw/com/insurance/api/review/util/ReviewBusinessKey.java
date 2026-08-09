package tw.com.insurance.api.review.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/** 建立不含個資原文、可重現且可供待審防重的業務鍵。 */
public final class ReviewBusinessKey {
	private ReviewBusinessKey() {
	}

	/** 將敏感識別值正規化後雜湊，避免待審鎖洩漏證件號碼。 */
	public static String sensitive(String prefix, String value) {
		try {
			String normalized = value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
			String hash = HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(normalized.getBytes(StandardCharsets.UTF_8)));
			return prefix + ":" + hash;
		} catch (Exception exception) {
			throw new IllegalStateException("覆核業務鍵建立失敗", exception);
		}
	}
}
