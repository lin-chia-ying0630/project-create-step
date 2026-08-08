package tw.com.insurance.batch.underwriting;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class UnderwritingBatchScheduler {
    static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");
    private final UnderwritingBatchService batchService;
    private final Clock clock;

    public UnderwritingBatchScheduler(UnderwritingBatchService batchService) {
        this(batchService, Clock.system(TAIPEI));
    }

    UnderwritingBatchScheduler(UnderwritingBatchService batchService, Clock clock) {
        this.batchService = batchService;
        this.clock = clock;
    }

    @Scheduled(cron = "${underwriting.batch.cron:0 0 21 * * *}", zone = "${underwriting.batch.zone:Asia/Taipei}")
    public void executeNightlyUnderwriting() {
        batchService.execute(LocalDate.now(clock), "SCHEDULED");
    }
}
