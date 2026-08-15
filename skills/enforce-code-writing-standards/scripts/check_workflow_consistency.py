#!/usr/bin/env python3
"""檢查根目錄 Workflow、README 與專案 Skills 的結構及路由是否一致。"""

from __future__ import annotations

import re
import sys
from pathlib import Path


FRONTMATTER_PATTERN = re.compile(r"\A---\n(.*?)\n---\n", re.DOTALL)
FIELD_PATTERN = re.compile(r"^([a-z_]+):\s*(.+)$", re.MULTILINE)
SKILL_LINK_PATTERN = re.compile(r"\]\(skills/([a-z0-9-]+)/SKILL\.md\)")
INLINE_SKILL_PATTERN = re.compile(r"`(?:skills/)?([a-z0-9]+(?:-[a-z0-9]+)+)(?:/SKILL\.md)?/?`")
FLOW_SKILL_PATTERN = re.compile(r"^\s*(?:→\s*)?([a-z0-9]+(?:-[a-z0-9]+)+)\s*$", re.MULTILINE)
MARKDOWN_LINK_PATTERN = re.compile(r"\[[^]]+\]\(([^)]+)\)")
OPENAI_FIELD_PATTERN = re.compile(
    r'^\s{2}(display_name|short_description|default_prompt):\s*"([^"]*)"\s*$',
    re.MULTILINE,
)

REQUIRED_PROHIBITION_HEADINGS = (
    "禁止假裝讀過規範",
    "禁止虛構專案內容",
    "禁止混入無法共用的業務邏輯",
    "禁止跨越分析與修改權限",
    "禁止不完整的跨層實作",
    "禁止虛假驗證與完成宣告",
    "禁止敏感資料與危險操作",
)

REQUIRED_PROHIBITION_GUARDS = (
    ("不得虛構不存在的專案內容", ("禁止建立或引用 repository 中不存在",)),
    ("分析與修改必須分離", ("禁止在分析、規格、審查或診斷任務中直接修改",)),
    ("發布動作必須另有授權", ("禁止未經明確要求執行 commit、push",)),
    ("不得以單層修改冒充跨層完成", ("禁止只修改資料庫、後端或前端其中一層",)),
    ("建置成功不等於功能完成", ("禁止把 compile 或 build 成功等同於功能完成",)),
    ("跳過或未執行不得列為通過", ("禁止把跳過、未執行、環境不可用",)),
    ("不得寫入敏感資料", ("禁止將正式個資、健康資料、財務資料",)),
    ("不得執行未確認範圍的破壞性操作", ("禁止在未確認範圍時執行刪除、覆寫",)),
)

REQUIRED_WORKFLOW_DESIGN_GUARDS = (
    ("單一主要 Workflow", ("單一入口模式",)),
    ("Skill 漸進載入", ("漸進載入模式",)),
    ("執行前契約閘門", ("契約閘門模式",)),
    ("結論必須有證據", ("證據閘門模式",)),
    ("修正迴圈必須有限", ("有限修正模式",)),
    ("缺少前提時失敗封閉", ("失敗封閉模式",)),
    ("完成結果必須可交接", ("交接模式",)),
)

SKILL_PROHIBITION_GUARDS = {
    "implement-full-stack-feature": (
        ("必須取得實作授權", ("明確要求實作",)),
        ("不得補造不存在的技術層", ("不得為湊齊清單建立",)),
        ("發布必須另外取得授權", ("只有使用者另外要求時才 commit",)),
    ),
    "fix-full-stack-bug": (
        ("必須先證明根因", ("先證明根因",)),
        ("診斷任務維持唯讀", ("單純要求診斷時只回報",)),
        ("資料庫修復維持向前 migration", ("forward-only migration",)),
    ),
    "review-generated-code": (
        ("審查預設唯讀", ("審查預設唯讀",)),
        ("審查不得直接修改", ("不直接修改檔案",)),
        ("審查意見必須有證據", ("沒有證據", "未證實推測")),
    ),
    "release-with-pr": (
        ("發布必須有明確授權", ("沒有使用者明確要求時",)),
        ("不得廣泛暫存", ("禁止 `git add -A`",)),
        ("不得強制推送", ("不使用 force push",)),
        ("建立 PR 不等於合併", ("建立 PR 與合併 PR 是不同動作",)),
    ),
    "diagnose-deployment": (
        ("部署診斷預設唯讀", ("診斷預設唯讀",)),
        ("不得因診斷直接修改產品或正式環境", ("不因排錯直接修改",)),
        ("未驗證不得宣稱通過", ("未驗證而非通過",)),
    ),
}


def add_error(errors: list[str], message: str) -> None:
    """加入不重複的繁中錯誤訊息，維持輸出穩定。"""
    if message not in errors:
        errors.append(message)


def read_text(path: Path, errors: list[str]) -> str:
    """讀取 UTF-8 文件；缺檔時記錄錯誤並回傳空字串。"""
    if not path.is_file():
        add_error(errors, f"缺少必要文件：{path}")
        return ""
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        add_error(errors, f"文件不是合法 UTF-8：{path}")
        return ""


def parse_frontmatter(skill_file: Path, errors: list[str]) -> dict[str, str]:
    """解析 SKILL.md frontmatter，限制只能包含 name 與 description。"""
    content = read_text(skill_file, errors)
    match = FRONTMATTER_PATTERN.match(content)
    if not match:
        add_error(errors, f"SKILL.md 缺少合法 frontmatter：{skill_file}")
        return {}
    fields = dict(FIELD_PATTERN.findall(match.group(1)))
    unknown_fields = sorted(set(fields) - {"name", "description"})
    if unknown_fields:
        add_error(errors, f"SKILL.md frontmatter 含不允許欄位：{skill_file} -> {', '.join(unknown_fields)}")
    if set(fields) != {"name", "description"}:
        add_error(errors, f"SKILL.md frontmatter 必須完整包含 name、description：{skill_file}")
    return fields


def validate_openai_yaml(skill_dir: Path, skill_name: str, errors: list[str]) -> None:
    """不用外部 YAML 套件，檢查 agents/openai.yaml 的必要介面欄位與預設提示。"""
    metadata_file = skill_dir / "agents" / "openai.yaml"
    content = read_text(metadata_file, errors)
    if not content:
        return
    fields = dict(OPENAI_FIELD_PATTERN.findall(content))
    required_fields = {"display_name", "short_description", "default_prompt"}
    missing_fields = sorted(required_fields - set(fields))
    if missing_fields:
        add_error(errors, f"openai.yaml 缺少介面欄位：{metadata_file} -> {', '.join(missing_fields)}")
    if fields.get("default_prompt") and f"${skill_name}" not in fields["default_prompt"]:
        add_error(errors, f"openai.yaml default_prompt 未引用 ${skill_name}：{metadata_file}")


def validate_skill_disclosure(skill_dir: Path, skill_content: str, errors: list[str]) -> None:
    """確認一層 references 都由 SKILL.md 直接連結，避免資源存在卻無法被漸進載入。"""
    references_dir = skill_dir / "references"
    if not references_dir.is_dir():
        return
    for reference in sorted(path for path in references_dir.iterdir() if path.is_file()):
        expected_link = f"references/{reference.name}"
        if expected_link not in skill_content:
            add_error(errors, f"SKILL.md 未直接連結 reference：{skill_dir / 'SKILL.md'} -> {expected_link}")


def validate_local_links(document: Path, repository_root: Path, errors: list[str]) -> None:
    """確認 Workflow、README 與 Skill 內的本機 Markdown 連結都能解析。"""
    content = read_text(document, errors)
    for target in MARKDOWN_LINK_PATTERN.findall(content):
        if target.startswith(("http://", "https://", "#")):
            continue
        relative_target = target.split("#", maxsplit=1)[0]
        if not relative_target:
            continue
        resolved = (document.parent / relative_target).resolve()
        try:
            resolved.relative_to(repository_root)
        except ValueError:
            add_error(errors, f"本機連結超出 repository：{document} -> {target}")
            continue
        if not resolved.exists():
            add_error(errors, f"本機連結不存在：{document} -> {target}")


def validate_required_guards(
    content: str,
    source: Path,
    requirements: tuple[tuple[str, tuple[str, ...]], ...],
    errors: list[str],
) -> None:
    """確認文件保留每項護欄語意，允許以核准的等價關鍵句表達。"""
    for label, alternatives in requirements:
        if not any(phrase in content for phrase in alternatives):
            add_error(errors, f"缺少禁止條款「{label}」：{source}")


def validate_prohibition_contracts(
    workflow_file: Path,
    workflow_text: str,
    skills_root: Path,
    errors: list[str],
) -> None:
    """檢查全域禁止項目與高風險 Workflow Skill 的授權及驗證邊界。"""
    for heading in REQUIRED_PROHIBITION_HEADINGS:
        if f"### {heading}" not in workflow_text:
            add_error(errors, f"WORKFLOWS.md 缺少禁止項目標題：{heading}")
    validate_required_guards(workflow_text, workflow_file, REQUIRED_PROHIBITION_GUARDS, errors)
    validate_required_guards(workflow_text, workflow_file, REQUIRED_WORKFLOW_DESIGN_GUARDS, errors)

    for skill_name, requirements in SKILL_PROHIBITION_GUARDS.items():
        skill_file = skills_root / skill_name / "SKILL.md"
        skill_text = read_text(skill_file, errors)
        validate_required_guards(skill_text, skill_file, requirements, errors)


def validate_repository(repository_root: Path) -> list[str]:
    """比對 Skill 實體、Workflow 路由、README 清單、metadata 與本機連結。"""
    errors: list[str] = []
    workflow_file = repository_root / "WORKFLOWS.md"
    readme_file = repository_root / "README.md"
    skills_root = repository_root / "skills"
    workflow_text = read_text(workflow_file, errors)
    readme_text = read_text(readme_file, errors)
    validate_prohibition_contracts(workflow_file, workflow_text, skills_root, errors)

    nested_workflows = sorted(skills_root.rglob("WORKFLOWS.md")) if skills_root.is_dir() else []
    for nested_workflow in nested_workflows:
        add_error(errors, f"WORKFLOWS.md 應放在 repository 根目錄，不得放入 Skill：{nested_workflow}")

    skill_dirs = (
        sorted(path for path in skills_root.iterdir() if path.is_dir() and not path.name.startswith("."))
        if skills_root.is_dir()
        else []
    )
    if not skill_dirs:
        add_error(errors, f"找不到任何專案 Skill：{skills_root}")

    skill_names: set[str] = set()
    skill_documents: list[Path] = []
    workflow_skill_names = set(INLINE_SKILL_PATTERN.findall(workflow_text)) | set(
        FLOW_SKILL_PATTERN.findall(workflow_text)
    )
    readme_skill_names = set(INLINE_SKILL_PATTERN.findall(readme_text))
    for skill_dir in skill_dirs:
        skill_file = skill_dir / "SKILL.md"
        fields = parse_frontmatter(skill_file, errors)
        skill_name = fields.get("name", skill_dir.name)
        description = fields.get("description", "")
        if skill_name in skill_names:
            add_error(errors, f"Skill frontmatter name 重複：{skill_name}")
        skill_names.add(skill_name)
        skill_documents.append(skill_file)
        if skill_name != skill_dir.name:
            add_error(errors, f"Skill 目錄與 frontmatter name 不一致：{skill_dir.name} != {skill_name}")
        if not re.search(r"\bUse (?:when|for|before)\b", description):
            add_error(errors, f"Skill description 未明確寫出 Use when／for／before 觸發情境：{skill_file}")
        if skill_name not in workflow_skill_names:
            add_error(errors, f"WORKFLOWS.md 未提供 Skill 路由：{skill_name}")
        if skill_name not in readme_skill_names:
            add_error(errors, f"README.md 未列出 Skill：{skill_name}")
        validate_openai_yaml(skill_dir, skill_name, errors)
        validate_skill_disclosure(skill_dir, read_text(skill_file, errors), errors)

    linked_workflows = set(SKILL_LINK_PATTERN.findall(workflow_text))
    for linked_skill in sorted(linked_workflows - skill_names):
        add_error(errors, f"WORKFLOWS.md 連結到不存在的 Skill：{linked_skill}")

    for document in [workflow_file, readme_file, *skill_documents]:
        validate_local_links(document, repository_root, errors)
    return sorted(errors)


def main() -> int:
    """執行一致性檢查並以 exit code 提供 CI 判定。"""
    repository_root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
    errors = validate_repository(repository_root)
    if errors:
        for error in errors:
            print(f"[ERROR] {error}")
        print(f"檢查失敗：共 {len(errors)} 項 Workflow／Skill 不一致。")
        return 1
    skill_count = sum(
        1
        for path in (repository_root / "skills").iterdir()
        if path.is_dir() and not path.name.startswith(".")
    )
    print(f"[OK] Workflow、README 與 {skill_count} 個 Skill 一致。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
