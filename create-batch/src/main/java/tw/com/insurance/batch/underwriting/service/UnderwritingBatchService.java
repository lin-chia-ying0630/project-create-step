package tw.com.insurance.batch.underwriting.service;

import java.time.LocalDate;

public interface UnderwritingBatchService {
	/** 依營業日及觸發方式執行一輪新契約核保批次。 */
	void execute(LocalDate businessDate, String triggerType);
}
