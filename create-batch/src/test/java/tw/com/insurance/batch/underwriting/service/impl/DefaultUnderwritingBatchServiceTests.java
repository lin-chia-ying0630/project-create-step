package tw.com.insurance.batch.underwriting.service.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tw.com.insurance.batch.underwriting.persistence.UnderwritingBatchMapper;

class DefaultUnderwritingBatchServiceTests {
	@Test
	void execute_givenExecutionDate_claimsOnlyThatDateAndCompletesEmptyExecution() {
		UnderwritingBatchMapper mapper = mock(UnderwritingBatchMapper.class);
		LocalDate executionDate = LocalDate.of(2026, 8, 10);
		when(mapper.findClaimedCandidates(anyString())).thenReturn(List.of());

		new DefaultUnderwritingBatchService(mapper).execute(executionDate, "SCHEDULED");

		verify(mapper).claimPendingRequests(anyString(), org.mockito.ArgumentMatchers.eq(executionDate));
		verify(mapper).completeExecution(anyString(), org.mockito.ArgumentMatchers.eq(0),
				org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(0),
				org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq("COMPLETED"));
	}

	@Test
	void execute_givenMatchedSubmittedPolicy_marksUnderwritingApproved() {
		UnderwritingBatchMapper mapper = mock(UnderwritingBatchMapper.class);
		Map<String, Object> candidate = new java.util.HashMap<>();
		candidate.put("batch_request_id", "TEST-BATCH-REQUEST-001");
		candidate.put("application_no", "TEST-APPLICATION-001");
		candidate.put("applicant_customer_id", "TEST-CUSTOMER-001");
		candidate.put("insured_customer_id", "TEST-CUSTOMER-002");
		candidate.put("product_code", "TEST-LIFE");
		candidate.put("product_version", "1.0");
		candidate.put("currency_code", "TWD");
		candidate.put("sum_assured_amount", new BigDecimal("1000000.0000"));
		candidate.put("premium_amount", new BigDecimal("12000.0000"));
		candidate.put("application_date", LocalDate.of(2026, 8, 9));
		candidate.put("requested_effective_date", LocalDate.of(2026, 8, 10));
		candidate.put("application_status", "SUBMITTED");
		candidate.put("initial_premium_match_status", "MATCHED");
		candidate.put("reserved_policy_no", "TEST-POLICY-001");
		when(mapper.findClaimedCandidates(anyString())).thenReturn(List.of(candidate));

		new DefaultUnderwritingBatchService(mapper).execute(LocalDate.of(2026, 8, 10), "SCHEDULED");

		verify(mapper).completeRequest("TEST-BATCH-REQUEST-001", "COMPLETED", "APPROVED");
		verify(mapper).updateApplicationValidation(org.mockito.ArgumentMatchers.eq("TEST-APPLICATION-001"),
				org.mockito.ArgumentMatchers.eq("PASS"), anyString());
		verify(mapper).completeExecution(anyString(), org.mockito.ArgumentMatchers.eq(1),
				org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(0),
				org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq("COMPLETED"));
	}
}
