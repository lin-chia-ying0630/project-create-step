#!/usr/bin/env python3
"""掃描可確定判斷的 Java、Vue 與 TypeScript 撰寫問題。"""

from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
errors: list[str] = []
warnings: list[str] = []

for source in root.rglob("*"):
    if not source.is_file() or any(part in {"node_modules", "target", "dist", ".git"} for part in source.parts):
        continue
    if source.suffix not in {".java", ".ts", ".vue"}:
        continue
    text = source.read_text(encoding="utf-8")
    relative = source.relative_to(root)
    if source.suffix == ".java":
        if re.search(r"import\s+[^;]+\.\*;", text):
            warnings.append(f"Java wildcard import：{relative}")
        if re.search(r"@Autowired\s+(?:private|protected|public)", text):
            errors.append(f"禁止 field injection：{relative}")
        if re.search(r"@(Select|Insert|Update|Delete)\b|\b(SELECT|INSERT|UPDATE|DELETE)\b", text, re.I):
            errors.append(f"Java 不得含 SQL：{relative}")
        if "printStackTrace(" in text:
            errors.append(f"禁止 printStackTrace：{relative}")
    if source.suffix in {".ts", ".vue"}:
        if re.search(r"\bany\b", text):
            warnings.append(f"檢查 TypeScript any：{relative}")
        if source.suffix == ".vue" and re.search(r"\b(fetch|axios)\s*\(", text):
            errors.append(f"Vue component 不得直接呼叫 HTTP：{relative}")
    for number, line in enumerate(text.splitlines(), 1):
        if len(line) > 140:
            warnings.append(f"行長超過 140：{relative}:{number}")

for item in errors:
    print(f"[ERROR] {item}")
for item in warnings:
    print(f"[WARN] {item}")
print(f"檢查完成：{len(errors)} errors，{len(warnings)} warnings")
raise SystemExit(1 if errors else 0)
