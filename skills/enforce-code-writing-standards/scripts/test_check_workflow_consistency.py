#!/usr/bin/env python3
"""測試 Workflow／Skill 一致性檢查器的關鍵失敗情境。"""

from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path


CHECKER_PATH = Path(__file__).with_name("check_workflow_consistency.py")
SPEC = importlib.util.spec_from_file_location("check_workflow_consistency", CHECKER_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError(f"無法載入一致性檢查器：{CHECKER_PATH}")
CHECKER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CHECKER)


class WorkflowConsistencyTest(unittest.TestCase):
    """驗證檢查器不會因模糊文字或壞檔案而誤判成功。"""

    def test_required_guard_reports_missing_clause(self) -> None:
        """禁止條款完全缺少時，每項必要護欄都必須產生錯誤。"""
        errors: list[str] = []

        CHECKER.validate_required_guards(
            "",
            Path("WORKFLOWS.md"),
            CHECKER.REQUIRED_PROHIBITION_GUARDS,
            errors,
        )

        self.assertEqual(len(CHECKER.REQUIRED_PROHIBITION_GUARDS), len(errors))

    def test_required_workflow_design_guard_reports_missing_pattern(self) -> None:
        """共用 Workflow 設計模式缺少時，每項必要模式都必須產生錯誤。"""
        errors: list[str] = []

        CHECKER.validate_required_guards(
            "",
            Path("WORKFLOWS.md"),
            CHECKER.REQUIRED_WORKFLOW_DESIGN_GUARDS,
            errors,
        )

        self.assertEqual(len(CHECKER.REQUIRED_WORKFLOW_DESIGN_GUARDS), len(errors))

    def test_invalid_utf8_is_reported_without_crashing(self) -> None:
        """非 UTF-8 文件應回報可理解錯誤，不得中斷整體掃描。"""
        with tempfile.TemporaryDirectory() as directory:
            document = Path(directory) / "SKILL.md"
            document.write_bytes(b"\xff\xfe")
            errors: list[str] = []

            content = CHECKER.read_text(document, errors)

        self.assertEqual("", content)
        self.assertEqual([f"文件不是合法 UTF-8：{document}"], errors)

    def test_skill_name_requires_explicit_workflow_code_span(self) -> None:
        """Skill 名稱須以 code span 出現，一般文字不得被視為有效路由。"""
        workflow_text = "一般文字 implement-full-stack-feature；路由 `fix-full-stack-bug`。"

        routed_skills = set(CHECKER.INLINE_SKILL_PATTERN.findall(workflow_text))

        self.assertNotIn("implement-full-stack-feature", routed_skills)
        self.assertIn("fix-full-stack-bug", routed_skills)

    def test_readme_skill_name_requires_inline_code(self) -> None:
        """README 必須以 code span 明確列出 Skill，避免一般敘述造成誤判。"""
        readme_text = "一般文字 java-refactor；技能清單 `review-generated-code`。"

        listed_skills = set(CHECKER.INLINE_SKILL_PATTERN.findall(readme_text))

        self.assertNotIn("java-refactor", listed_skills)
        self.assertIn("review-generated-code", listed_skills)

    def test_plain_text_flow_recognizes_only_standalone_skill_lines(self) -> None:
        """純文字流程只接受獨立 Skill 行，不把敘述中的名稱誤認為路由。"""
        workflow_text = "legacy-code-explainer\n    → spec-generator\n提到 impact-analysis 但不是路由"

        routed_skills = set(CHECKER.FLOW_SKILL_PATTERN.findall(workflow_text))

        self.assertEqual({"legacy-code-explainer", "spec-generator"}, routed_skills)

    def test_unlinked_reference_is_rejected(self) -> None:
        """reference 存在但入口未連結時必須回報，避免漸進載入斷鏈。"""
        with tempfile.TemporaryDirectory() as directory:
            skill_dir = Path(directory) / "example-skill"
            references_dir = skill_dir / "references"
            references_dir.mkdir(parents=True)
            (references_dir / "details.md").write_text("細節", encoding="utf-8")
            errors: list[str] = []

            CHECKER.validate_skill_disclosure(skill_dir, "# Example", errors)

        self.assertEqual(1, len(errors))
        self.assertIn("references/details.md", errors[0])


if __name__ == "__main__":
    unittest.main()
