-- 將既有核保批次資料表的中文業務名稱統一為「新契約批次承保作業」；英文實體名稱維持相容。
ALTER TABLE new_contract.underwriting_batch_request
    COMMENT = '新契約批次承保作業排程案件';

ALTER TABLE new_contract.underwriting_batch_execution
    COMMENT = '新契約批次承保作業執行紀錄';
