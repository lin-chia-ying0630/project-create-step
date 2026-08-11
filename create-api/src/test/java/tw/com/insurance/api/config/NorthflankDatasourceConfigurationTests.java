package tw.com.insurance.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

/** 驗證 Northflank Addon 與本機 Docker 共用的 datasource 環境變數契約。 */
class NorthflankDatasourceConfigurationTests {

	/** Northflank 注入的 JDBC URI、帳密與 PORT 必須優先於本機 fallback。 */
	@Test
	void shouldResolveNorthflankAddonSecretsBeforeLocalFallback() throws IOException {
		StandardEnvironment environment = environmentWith(Map.of(
				"MYSQL_JDBC_URI", "jdbc:mysql://mysql.internal:3306/new_contract?sslMode=REQUIRED",
				"MYSQL_USERNAME", "northflank-app",
				"MYSQL_PASSWORD", "northflank-secret",
				"PORT", "9090"));

		assertThat(environment.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:mysql://mysql.internal:3306/new_contract?sslMode=REQUIRED");
		assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("northflank-app");
		assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("northflank-secret");
		assertThat(environment.getProperty("spring.flyway.user")).isEqualTo("northflank-app");
		assertThat(environment.getProperty("spring.flyway.password")).isEqualTo("northflank-secret");
		assertThat(environment.getProperty("server.port")).isEqualTo("9090");
	}

	/** 既有 Docker Compose 提供 DB_URL 時仍須維持原本的本機連線方式。 */
	@Test
	void shouldKeepDockerDatasourceFallback() throws IOException {
		StandardEnvironment environment = environmentWith(Map.of(
				"DB_URL", "jdbc:mysql://mysql:3306/new_contract",
				"DB_USER", "insurance",
				"DB_PASSWORD", "local-only"));

		assertThat(environment.getProperty("spring.datasource.url"))
				.isEqualTo("jdbc:mysql://mysql:3306/new_contract");
		assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("insurance");
		assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("local-only");
	}

	/** 載入正式 application.yml，並將測試環境變數放在最高優先序。 */
	private StandardEnvironment environmentWith(Map<String, Object> variables) throws IOException {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().addFirst(new MapPropertySource("test-environment", variables));
		new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))
				.forEach(environment.getPropertySources()::addLast);
		return environment;
	}
}
