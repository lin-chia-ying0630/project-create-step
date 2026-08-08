package tw.com.insurance.api.newcontract;

import static tw.com.insurance.api.newcontract.NewContractDtos.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.insurance.api.common.BusinessException;

@Service
public class NewContractService {
    private final NewContractMapper mapper;

    public NewContractService(NewContractMapper mapper) { this.mapper = mapper; }

    @Transactional
    public CreateApplicationResult createApplication(CreateApplicationRequest request) {
        if (request.requestedEffectiveDate().isBefore(request.applicationDate()))
            throw new BusinessException("NCT-4001", "預定生效日不得早於要保日期");
        String applicationId=UUID.randomUUID().toString(), dueId=UUID.randomUUID().toString();
        try {
            mapper.insertApplication(applicationId,request.applicationNo(),request.applicationDate(),
                request.channelCode(),request.productCode(),request.productVersion(),request.currencyCode(),
                request.sumAssuredAmount(),request.premiumAmount(),request.paymentModeCode(),request.requestedEffectiveDate());
            mapper.insertPremiumDue(dueId,request.applicationNo(),request.currencyCode(),request.premiumAmount());
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("NCT-4091", "要保書號碼已存在");
        }
        return new CreateApplicationResult(applicationId,request.applicationNo(),"SUBMITTED",dueId,
            request.premiumAmount(),request.currencyCode());
    }

    public PremiumDuePreview getPremiumDue(String applicationNo) {
        Map<String,Object> row = mapper.findPremiumDue(applicationNo);
        if (row == null) throw new BusinessException("NCT-4041", "查無待繳首期保費的要保案件");
        return new PremiumDuePreview(text(row,"application_no"), text(row,"premium_due_id"),
            text(row,"currency_code"), decimal(row,"calculated_premium_amount"),
            text(row,"calculation_rule_version"), text(row,"due_status"));
    }

    @Transactional
    public PremiumMatchResult matchPremium(RemittanceSlipRequest request) {
        PremiumDuePreview due = getPremiumDue(request.applicationNo());
        BigDecimal difference = request.actualPaidAmount().subtract(due.calculatedPremiumAmount());
        String status;
        String reason = null;
        if (!due.currencyCode().equals(request.currencyCode())) { status="CURRENCY_MISMATCH"; reason="CURRENCY_MISMATCH"; }
        else if (difference.signum() < 0) { status="UNDERPAID"; reason="AMOUNT_UNDERPAID"; }
        else if (difference.signum() > 0) { status="OVERPAID"; reason="AMOUNT_OVERPAID"; }
        else status="MATCHED";
        String slipId=UUID.randomUUID().toString(), matchId=UUID.randomUUID().toString();
        try {
            mapper.insertRemittance(slipId,request.remittanceSlipNo(),request.applicationNo(),
                request.paymentMethodCode(),request.paymentReference(),request.currencyCode(),
                request.actualPaidAmount(),status.equals("MATCHED")?"MATCHED":"RECEIVED",request.paidAt(),
                request.payerRelationshipCode());
            mapper.insertPremiumMatch(matchId,due.premiumDueId(),slipId,due.calculatedPremiumAmount(),
                request.actualPaidAmount(),difference,request.currencyCode(),status,reason);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("PAY-4091", "送金單、交易參考號或應繳紀錄已完成配對");
        }
        if (status.equals("MATCHED")) mapper.updateDueStatus(due.premiumDueId(),"MATCHED");
        mapper.updateApplicationMatch(request.applicationNo(),status);
        return new PremiumMatchResult(matchId,status,due.calculatedPremiumAmount(),request.actualPaidAmount(),
            difference,status.equals("MATCHED"));
    }

    @Transactional
    public UnderwritingBatchRequestResult enqueue(UnderwritingBatchRequest request) {
        if (mapper.countApplication(request.applicationNo()) == 0)
            throw new BusinessException("NCT-4042", "查無要保案件");
        String id=UUID.randomUUID().toString();
        try { mapper.insertBatchRequest(id,request.applicationNo(),request.requestedBusinessDate()); }
        catch (DuplicateKeyException exception) { throw new BusinessException("BAT-4091", "此案件已排入指定營業日批次"); }
        return new UnderwritingBatchRequestResult(id,request.applicationNo(),"PENDING",
            LocalDateTime.of(request.requestedBusinessDate(), LocalTime.of(21,0)));
    }

    public List<UnderwritingBatchExecutionSummary> latestExecutions() {
        return mapper.findLatestExecutions().stream().map(row -> new UnderwritingBatchExecutionSummary(
            text(row,"batch_execution_id"),localDate(row.get("business_date")),text(row,"execution_status"),
            localDateTime(row.get("started_at")),localDateTime(row.get("completed_at")),number(row,"total_count"),
            number(row,"approved_count"),number(row,"inquiry_count"),number(row,"failed_count"))).toList();
    }

    public PolicyReversalPreview previewReversal(String policyNo) {
        Map<String,Object> row=mapper.findPolicyForReversal(policyNo);
        if(row==null) throw new BusinessException("POL-4041","查無可撤回的正式保單");
        List<String> blockers="PENDING".equals(text(row,"policy_status")) ? List.of()
            : List.of("保單狀態不是 PENDING，可能已生效或已有權利義務，禁止直接刪除");
        Map<String,Integer> counts=new LinkedHashMap<>();
        counts.put("main.policy_underwriting_condition",0); counts.put("main.policy_beneficiary",0);
        counts.put("main.policy_coverage",0); counts.put("main.policy_party",0);
        counts.put("main.policy_contract",mapper.countPolicy(policyNo));
        long policyVersion=longNumber(row,"policy_version"), appVersion=longNumber(row,"application_version"),
            uwVersion=longNumber(row,"underwriting_version");
        return new PolicyReversalPreview(policyNo,text(row,"application_no"),text(row,"underwriting_case_no"),
            text(row,"policy_status"),text(row,"application_status"),text(row,"underwriting_status"),
            localDate(row.get("effective_date")),policyVersion,appVersion,uwVersion,counts,blockers,
            hash(policyNo+":"+policyVersion+":"+appVersion+":"+uwVersion));
    }

    @Transactional
    public PolicyReversalResult reverse(PolicyReversalRequest request, String requestId) {
        PolicyReversalPreview preview=previewReversal(request.policyNo());
        if(!preview.blockers().isEmpty()) throw new BusinessException("POL-4091",preview.blockers().get(0));
        if(preview.policyVersion()!=request.expectedPolicyVersion()
            || preview.applicationVersion()!=request.expectedApplicationVersion()
            || preview.underwritingVersion()!=request.expectedUnderwritingVersion()
            || !preview.confirmToken().equals(request.confirmToken()))
            throw new BusinessException("POL-4092","資料已被其他人異動，請重新查詢");
        String auditId=UUID.randomUUID().toString();
        String before="{\"policyNo\":\""+preview.policyNo()+"\",\"policyStatus\":\""+preview.policyStatus()+
            "\",\"applicationStatus\":\""+preview.applicationStatus()+"\",\"underwritingStatus\":\""+
            preview.underwritingStatus()+"\"}";
        String after="{\"policyDeleted\":true,\"applicationStatus\":\"SUBMITTED\",\"underwritingStatus\":\"PENDING\"}";
        if(mapper.deletePolicy(request.policyNo(),request.expectedPolicyVersion())!=1
            || mapper.resetApplication(preview.applicationNo(),request.expectedApplicationVersion())!=1
            || mapper.resetUnderwriting(preview.underwritingCaseNo(),request.expectedUnderwritingVersion())!=1)
            throw new BusinessException("POL-4092","資料已被其他人異動，請重新查詢");
        mapper.insertReversalAudit(auditId,request.policyNo(),preview.applicationNo(),preview.underwritingCaseNo(),
            request.reasonCode(),request.reasonDescription(),requestId,before,after,hash(before),hash(after));
        return new PolicyReversalResult(auditId,request.policyNo(),preview.applicationNo(),"SUBMITTED","PENDING");
    }

    private static String text(Map<String,Object> row,String key){return String.valueOf(row.get(key));}
    private static BigDecimal decimal(Map<String,Object> row,String key){return (BigDecimal)row.get(key);}
    private static int number(Map<String,Object> row,String key){return ((Number)row.get(key)).intValue();}
    private static long longNumber(Map<String,Object> row,String key){return ((Number)row.get(key)).longValue();}
    private static LocalDate localDate(Object value){return value instanceof Date d?d.toLocalDate():(LocalDate)value;}
    private static LocalDateTime localDateTime(Object value){return value==null?null:value instanceof Timestamp t?t.toLocalDateTime():(LocalDateTime)value;}
    private static String hash(String value){
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception exception){throw new IllegalStateException(exception);}
    }
}
