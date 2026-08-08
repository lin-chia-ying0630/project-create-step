package tw.com.insurance.batch.underwriting;

import java.time.LocalDate;

public interface UnderwritingBatchService {
    void execute(LocalDate businessDate, String triggerType);
}
