INSERT INTO new_contract.code_definition
  (code_group, code_group_description_zh_tw, code_field, code_field_description_zh_tw,
   code_value, code_description_zh_tw, code_description_en, display_order, active_flag, effective_from,
   source_system, source_version)
VALUES
  ('initial-premium', '首期保險費收款', 'payment_channel_code', '繳費管道',
   'BANK_TRANSFER', '銀行轉帳', 'Bank transfer', 1, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payment_channel_code', '繳費管道',
   'CREDIT_CARD', '信用卡', 'Credit card', 2, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payment_channel_code', '繳費管道',
   'CASH', '現金', 'Cash', 3, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payment_channel_code', '繳費管道',
   'CONVENIENCE_STORE', '超商代收', 'Convenience store collection', 4, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payment_channel_code', '繳費管道',
   'DIRECT_DEBIT', '帳戶扣款', 'Direct debit', 5, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payer_role_code', '繳款人身分',
   'APPLICANT', '要保人', 'Applicant', 1, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payer_role_code', '繳款人身分',
   'INSURED', '被保險人', 'Insured', 2, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payer_role_code', '繳款人身分',
   'BENEFICIARY', '受益人', 'Beneficiary', 3, 'Y', '2026-08-09', '新契約首期收費作業', '1.0'),
  ('initial-premium', '首期保險費收款', 'payer_role_code', '繳款人身分',
   'OTHER', '其他關係人', 'Other related party', 4, 'Y', '2026-08-09', '新契約首期收費作業', '1.0');
