-- 覆核案件使用單碼狀態：P 待覆核／處理中、A 覆核核准、R 覆核退回。
ALTER TABLE new_contract.business_review_case DROP CHECK chk_business_review_decision;
ALTER TABLE new_contract.business_review_case DROP CHECK chk_business_review_status;

UPDATE new_contract.business_review_case
   SET review_status = CASE review_status
       WHEN 'PENDING' THEN 'P'
       WHEN 'APPROVED' THEN 'A'
       WHEN 'REJECTED' THEN 'R'
       ELSE review_status END;

ALTER TABLE new_contract.business_review_case
  MODIFY COLUMN review_status CHAR(1) NOT NULL COMMENT '覆核狀態：P待覆核／處理中、A覆核核准、R覆核退回',
  ADD CONSTRAINT chk_business_review_status CHECK (review_status IN ('P','A','R')),
  ADD CONSTRAINT chk_business_review_decision CHECK (
    (review_status = 'P' AND reviewer_id IS NULL AND reviewed_at IS NULL)
    OR (review_status IN ('A','R') AND reviewer_id IS NOT NULL AND reviewed_at IS NOT NULL)
  );
