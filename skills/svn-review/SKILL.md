---
name: svn-review
description: Review SVN revisions, branches, diffs, merge history, and working-copy changes to explain intent, defects, and impact. Use when comparing revision numbers, auditing a release, checking merge scope, or reviewing legacy repositories maintained with Subversion.
---

# SVN 版本審查

## 工作流程

1. 確認 repository URL、working-copy path、peg revision、比較 revisions 與 branch／tag 範圍。
2. 以唯讀方式檢查 status、diff、log、changed paths、copy history 與 mergeinfo。
3. 依 business capability 分組變更，不依檔案順序流水帳。
4. 沿 caller、SQL、configuration、deployment descriptor 與 test 追蹤契約影響。
5. 標示 missing file、partial merge、generated artifact、secret 與 unrelated change。

## 限制

- 除非使用者明確要求 merge、revert、resolve 或 commit，審查保持唯讀。
- 區分 repository revision 與本機 unversioned／modified file。
- 每個 finding 引用 revision 與 path；不得只依 log message 推測作者意圖。

## 產出

先列出依嚴重度排序的 actionable findings，再摘要 revision scope、impact、test gap 與 release risk。沒有可執行問題時明確說明。
