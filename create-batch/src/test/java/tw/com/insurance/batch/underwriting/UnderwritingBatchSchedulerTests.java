package tw.com.insurance.batch.underwriting;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UnderwritingBatchSchedulerTests {
    @Test void 每晚排程入口使用台北營業日() {
        var service = mock(UnderwritingBatchService.class);
        var clock = Clock.fixed(Instant.parse("2026-08-08T13:00:00Z"), ZoneId.of("Asia/Taipei"));
        new UnderwritingBatchScheduler(service, clock).executeNightlyUnderwriting();
        verify(service).execute(java.time.LocalDate.of(2026, 8, 8), "SCHEDULED");
    }
}
