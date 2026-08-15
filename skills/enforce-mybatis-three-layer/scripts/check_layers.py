#!/usr/bin/env python3
"""檢查指定 Java package root 下 MyBatis package-by-feature 的明顯分層違規。"""

from pathlib import Path
import re
import sys


sql_pattern = re.compile(r"@(Select|Insert|Update|Delete)\b|\b(JdbcTemplate|Connection|Statement)\b")
java_sql_keyword_pattern = re.compile(r"\b(SELECT|INSERT|UPDATE|DELETE)\b", re.IGNORECASE)


def validate_layers(root: Path) -> list[str]:
    """掃描指定 package root，回傳所有可確定的分層違規。"""
    errors: list[str] = []
    for feature in sorted(path for path in root.iterdir() if path.is_dir() and path.name != "common"):
        for source in feature.glob("*.java"):
            errors.append(f"功能根目錄不得直接放 Java class：{source}")

    for source in root.rglob("*.java"):
        relative = source.relative_to(root)
        content = source.read_text(encoding="utf-8")
        parts = relative.parts
        if "controller" in parts:
            if ".persistence." in content or re.search(r"\b\w+Mapper\b", content):
                errors.append(f"Controller 不得依賴 Mapper：{relative}")
            if sql_pattern.search(content):
                errors.append(f"Controller 不得含 SQL/JDBC：{relative}")
        if "service" in parts and sql_pattern.search(content):
            errors.append(f"Service 不得含 SQL/JDBC：{relative}")
        if "persistence" in parts and "ResponseBodyDto" in content:
            errors.append(f"Mapper 不得依賴 API response wrapper：{relative}")
        if "persistence" in parts and re.search(r"@(Select|Insert|Update|Delete)\b", content):
            errors.append(f"Java Mapper 不得使用 SQL annotation，請移至 XML：{relative}")
        if java_sql_keyword_pattern.search(content):
            errors.append(f"Java 程式不得直接包含 SQL 語法，請移至 MyBatis XML：{relative}")
    return errors


def main() -> int:
    """要求明確 package root 後執行掃描，避免搬移後默默檢查錯誤目錄。"""
    if len(sys.argv) != 2:
        print("用法：python3 check_layers.py <backend-module>/src/main/java/<java-package-root>")
        return 2
    root = Path(sys.argv[1])
    if not root.is_dir():
        print(f"[ERROR] 找不到 Java package 根目錄：{root}")
        return 2
    errors = validate_layers(root)
    if errors:
        print("[FAIL] MyBatis 三層架構檢查失敗")
        for error in errors:
            print(f"- {error}")
        return 1
    print(f"[OK] MyBatis 三層架構檢查通過：{root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
