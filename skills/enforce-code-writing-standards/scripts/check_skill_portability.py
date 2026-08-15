#!/usr/bin/env python3
"""掃描 Skills 是否殘留來源專案識別、絕對路徑或固定模組名稱。"""

from __future__ import annotations

import re
import sys
from pathlib import Path


PORTABILITY_PATTERNS = (
    ("來源 repository 名稱", re.compile("project" + "-create-step", re.IGNORECASE)),
    ("舊 POS repository 名稱", re.compile("pos" + r"-(?:api|web|project)\b", re.IGNORECASE)),
    ("固定 create module 名稱", re.compile("create" + r"-(?:api|web|batch)\b", re.IGNORECASE)),
    ("來源專案 schema", re.compile("new" + r"[_-]contract", re.IGNORECASE)),
    ("來源機器絕對路徑", re.compile("/" + r"Users/|Idea" + "Projects/")),
    ("固定 Java package root", re.compile("tw/" + r"com/insurance/api")),
    ("舊前端 shared 路徑", re.compile("src/" + r"components/shared")),
)
TEXT_SUFFIXES = {".md", ".py", ".yaml", ".yml"}
SELF_FILES = {"check_skill_portability.py", "test_check_skill_portability.py"}


def find_portability_errors(skills_root: Path) -> list[str]:
    """回傳 Skills 內不應直接搬到下一個專案的來源專案殘留。"""
    errors: list[str] = []
    if not skills_root.is_dir():
        return [f"找不到 Skills 目錄：{skills_root}"]
    for document in sorted(skills_root.rglob("*")):
        if not document.is_file() or document.suffix not in TEXT_SUFFIXES or document.name in SELF_FILES:
            continue
        try:
            content = document.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            errors.append(f"文件不是合法 UTF-8：{document}")
            continue
        for label, pattern in PORTABILITY_PATTERNS:
            for match in pattern.finditer(content):
                line = content.count("\n", 0, match.start()) + 1
                errors.append(f"{label}：{document}:{line} -> {match.group(0)}")
    return errors


def main() -> int:
    """執行可搬移性掃描並以 exit code 提供自動化判定。"""
    repository_root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    errors = find_portability_errors(repository_root / "skills")
    if errors:
        for error in errors:
            print(f"[ERROR] {error}")
        print(f"檢查失敗：Skills 含 {len(errors)} 項來源專案殘留。")
        return 1
    print("[OK] Skills 未發現來源專案名稱、固定模組、絕對路徑或舊 package 殘留。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
