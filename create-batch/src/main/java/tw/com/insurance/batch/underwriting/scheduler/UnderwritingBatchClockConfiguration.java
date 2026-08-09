package tw.com.insurance.batch.underwriting.scheduler;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 集中提供核保排程的臺北時區時鐘，避免 Service 與排程器各自判斷日期。 */
@Configuration
public class UnderwritingBatchClockConfiguration {
	/** 正式環境以 Asia/Taipei 產生批次執行日；測試可直接改用固定 Clock。 */
	@Bean
	Clock underwritingBatchClock() {
		return Clock.system(UnderwritingBatchScheduler.TAIPEI);
	}
}
