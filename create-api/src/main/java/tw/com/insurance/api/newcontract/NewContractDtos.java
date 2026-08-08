package tw.com.insurance.api.newcontract;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class NewContractDtos {
    private NewContractDtos() {}

    public record PremiumDuePreview(String applicationNo, String premiumDueId, String currencyCode,
                                    @JsonFormat(shape=JsonFormat.Shape.STRING) BigDecimal calculatedPremiumAmount, String calculationRuleVersion,
                                    String dueStatus) {}
    public record RemittanceSlipRequest(@NotBlank @Size(max=32) String applicationNo,
        @NotBlank @Size(max=50) String remittanceSlipNo, @NotBlank @Size(max=20) String paymentMethodCode,
        @NotBlank @Size(max=100) String paymentReference, @NotBlank @Size(min=3,max=3) String currencyCode,
        @NotNull @DecimalMin("0.0001") BigDecimal actualPaidAmount, @NotNull LocalDateTime paidAt,
        @Size(max=20) String payerRelationshipCode) {}
    public record PremiumMatchResult(String premiumMatchId, String matchStatus,
        @JsonFormat(shape=JsonFormat.Shape.STRING) BigDecimal expectedAmount,
        @JsonFormat(shape=JsonFormat.Shape.STRING) BigDecimal actualAmount,
        @JsonFormat(shape=JsonFormat.Shape.STRING) BigDecimal differenceAmount, boolean mayUnderwrite) {}
    public record UnderwritingBatchRequest(@NotBlank @Size(max=32) String applicationNo,
                                            @NotNull LocalDate requestedBusinessDate) {}
    public record UnderwritingBatchRequestResult(String batchRequestId, String applicationNo,
                                                  String requestStatus, LocalDateTime scheduledAt) {}
    public record UnderwritingBatchExecutionSummary(String batchExecutionId, LocalDate businessDate,
        String executionStatus, LocalDateTime startedAt, LocalDateTime completedAt, int totalCount,
        int approvedCount, int inquiryCount, int failedCount) {}
    public record PolicyReversalPreview(String policyNo, String applicationNo, String underwritingCaseNo,
        String policyStatus, String applicationStatus, String underwritingStatus, LocalDate effectiveDate,
        long policyVersion, long applicationVersion, long underwritingVersion,
        Map<String,Integer> deleteCounts, List<String> blockers, String confirmToken) {}
    public record PolicyReversalRequest(@NotBlank @Size(max=32) String policyNo,
        @NotBlank @Size(max=32) String reasonCode, @NotBlank @Size(min=10,max=500) String reasonDescription,
        long expectedPolicyVersion, long expectedApplicationVersion, long expectedUnderwritingVersion,
        @NotBlank String confirmToken) {}
    public record CreateApplicationRequest(@NotBlank @Size(max=32) String applicationNo,
        @NotNull LocalDate applicationDate, @NotBlank @Size(max=20) String channelCode,
        @NotBlank @Size(max=32) String productCode, @NotBlank @Size(max=32) String productVersion,
        @NotBlank @Size(min=3,max=3) String currencyCode, @NotNull @DecimalMin("0.0001") BigDecimal sumAssuredAmount,
        @NotNull @DecimalMin("0.0000") BigDecimal premiumAmount, @NotBlank @Size(max=16) String paymentModeCode,
        @NotNull LocalDate requestedEffectiveDate) {}
    public record CreateApplicationResult(String applicationId, String applicationNo, String applicationStatus,
        String premiumDueId, @JsonFormat(shape=JsonFormat.Shape.STRING) BigDecimal calculatedPremiumAmount, String currencyCode) {}
    public record PolicyReversalResult(String reversalAuditId, String policyNo, String applicationNo,
                                        String applicationStatus, String underwritingStatus) {}
}
