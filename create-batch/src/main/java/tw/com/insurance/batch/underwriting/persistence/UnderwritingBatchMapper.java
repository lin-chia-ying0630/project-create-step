package tw.com.insurance.batch.underwriting.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 核保批次排程領件、結果與稽核的持久層契約。 */
@Mapper
public interface UnderwritingBatchMapper {
	/** 先建立 RUNNING 執行紀錄，讓後續領件及稽核共用同一個批次識別碼。 */
	int insertExecution(@Param("executionId") String executionId, @Param("executionDate") LocalDate executionDate,
			@Param("triggerType") String triggerType);

	/** 僅領取狀態為 PENDING 且指定執行日完全相同的案件，避免提前處理未到期排程。 */
	int claimPendingRequests(@Param("executionId") String executionId, @Param("executionDate") LocalDate executionDate);

	/** 讀取本次已領取案件的不可變核保快照；不得混入其他執行批次的案件。 */
	List<Map<String, Object>> findClaimedCandidates(@Param("executionId") String executionId);

	/** 保存承保或照會結果；同一要保案件重跑時更新既有核保案件而不建立第二筆。 */
	int upsertUnderwritingCase(@Param("caseNo") String caseNo, @Param("applicationNo") String applicationNo,
			@Param("status") String status, @Param("decisionCode") String decisionCode,
			@Param("reasonCode") String reasonCode, @Param("policyNo") String policyNo);

	/** 將單筆排程案件由 RUNNING 結束為 COMPLETED 或 INQUIRY，並保存主要結果代碼。 */
	int completeRequest(@Param("requestId") String requestId, @Param("status") String status,
			@Param("resultCode") String resultCode);

	/** 保存本次核保檢核 PASS／FAIL 與所屬批次，供後續查詢追溯。 */
	int updateApplicationValidation(@Param("applicationNo") String applicationNo, @Param("status") String status,
			@Param("executionId") String executionId);

	/** 在同一交易追加核保批次成功稽核，不記錄個資、健康告知或付款工具內容。 */
	int insertAudit(@Param("auditId") String auditId, @Param("executionId") String executionId,
			@Param("applicationNo") String applicationNo, @Param("caseNo") String caseNo,
			@Param("resultCode") String resultCode);

	/** 依實際處理結果彙總批次件數並結束 RUNNING 執行紀錄。 */
	int completeExecution(@Param("executionId") String executionId, @Param("totalCount") int totalCount,
			@Param("approvedCount") int approvedCount, @Param("inquiryCount") int inquiryCount,
			@Param("failedCount") int failedCount, @Param("executionStatus") String executionStatus);
}
