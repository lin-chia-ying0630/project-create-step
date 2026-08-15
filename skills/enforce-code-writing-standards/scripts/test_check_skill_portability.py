#!/usr/bin/env python3
"""測試 Skill 可搬移性掃描器。"""

from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path


CHECKER_PATH = Path(__file__).with_name("check_skill_portability.py")
SPEC = importlib.util.spec_from_file_location("check_skill_portability", CHECKER_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"無法載入可搬移性檢查器：{CHECKER_PATH}")
CHECKER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CHECKER)


class SkillPortabilityTest(unittest.TestCase):
    """確認來源專案殘留會被阻擋，明確佔位符則可通過。"""

    def test_project_specific_module_is_rejected(self) -> None:
        """固定來源模組出現在 Skill 時必須回報位置。"""
        with tempfile.TemporaryDirectory() as directory:
            skills_root = Path(directory) / "skills"
            skill_dir = skills_root / "example-skill"
            skill_dir.mkdir(parents=True)
            (skill_dir / "SKILL.md").write_text("執行 create" + "-api/mvnw test", encoding="utf-8")

            errors = CHECKER.find_portability_errors(skills_root)

        self.assertEqual(1, len(errors))
        self.assertIn("固定 create module 名稱", errors[0])

    def test_explicit_placeholders_are_allowed(self) -> None:
        """可在目標專案替換的 module 與 package 佔位符不應被阻擋。"""
        with tempfile.TemporaryDirectory() as directory:
            skills_root = Path(directory) / "skills"
            skill_dir = skills_root / "example-skill"
            skill_dir.mkdir(parents=True)
            (skill_dir / "SKILL.md").write_text(
                "<backend-module>/src/main/java/<java-package-root>",
                encoding="utf-8",
            )

            errors = CHECKER.find_portability_errors(skills_root)

        self.assertEqual([], errors)


if __name__ == "__main__":
    unittest.main()
