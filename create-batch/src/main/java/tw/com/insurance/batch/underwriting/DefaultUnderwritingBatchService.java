package tw.com.insurance.batch.underwriting;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DefaultUnderwritingBatchService implements UnderwritingBatchService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUnderwritingBatchService.class);

    @Override
    public void execute(LocalDate businessDate, String triggerType) {
        // 下一個垂直切片由 MyBatis coordinator 領取 queue 並逐件交易；此處只保證排程入口可啟動。
        LOG.info("核保批次已觸發 businessDate={}, triggerType={}", businessDate, triggerType);
    }
}
