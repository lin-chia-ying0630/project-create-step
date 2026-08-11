/** 後端動態代碼定義的唯讀顯示契約。 */
export interface CodeDefinitionOption {
  code: string
  description: string
  descriptionEn: string | null
  classificationCode: string | null
  classificationDescription: string | null
  breakdownCode: string | null
  breakdownDescription: string | null
  natureOfWork: string | null
  sourceVersion: string | null
}

/** 可供畫面選擇的代碼表群組與欄位。 */
export interface CodeDefinitionTableOption {
  codeGroup: string
  codeGroupDescription: string
  codeField: string
  codeFieldDescription: string
}
