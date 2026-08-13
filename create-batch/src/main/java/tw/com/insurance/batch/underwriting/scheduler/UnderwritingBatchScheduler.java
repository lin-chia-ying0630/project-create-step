package tw.com.insurance.batch.underwriting.scheduler;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tw.com.insurance.batch.underwriting.service.UnderwritingBatchService;

@Component
public class UnderwritingBatchScheduler {
	static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");
	private final UnderwritingBatchService batchService;
	private final Clock clock;

	/** 建立使用臺北時區 Clock 的排程器；測試可注入固定 Clock 驗證日界線。 */
	public UnderwritingBatchScheduler(UnderwritingBatchService batchService, Clock clock) {
		this.batchService = batchService;
		this.clock = clock;
	}

	@Scheduled(cron = "${underwriting.batch.cron:0 0 21 * * *}", zone = "${underwriting.batch.zone:Asia/Taipei}")
	/** 每晚九點以當日臺北營業日觸發自動核保。 */
	public void executeNightlyUnderwriting() {
		batchService.execute(LocalDate.now(clock), "SCHEDULED");
	}
}
