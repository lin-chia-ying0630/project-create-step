---
name: enforce-mybatis-three-layer
description: Establish, refactor, or review a Spring Boot MyBatis backend using package-by-feature and strict Controller, Service, and Persistence boundaries. Use when creating a Java feature, adding an API or mapper, reorganizing packages, reviewing a mixed or flat directory, or checking that SQL, transactions, DTOs, and dependencies are in the correct layer.
---

# 強制 MyBatis 三層架構

以「業務功能優先、功能內分層」整理 Spring Boot + MyBatis 程式。不得只搬檔案；必須同時修正 package、import、依賴方向、交易邊界與測試。

## 執行流程

1. 讀取根目錄 `AGENTS.md`、`.github/copilot-instructions.md` 與既有 package。
2. 依業務能力決定 `<feature>`，例如 `customer`、`newcontract`、`inquiry`、`premium`；不得以 `controller` 作為整個系統的第一層目錄。
3. 依 [references/package-layout.md](references/package-layout.md) 建立目錄與依賴。
4. 先定義 Service interface，再由 Controller 依賴 interface，由 `service/impl` 實作並注入 Mapper。
5. 將 SQL 全部移到 `src/main/resources/mapper/<feature>/*Mapper.xml`；Java Mapper 只留 method contract，所有輸入使用 `#{}` 綁定。
6. 將多表寫入的完整業務動作放在同一個 Service implementation 公開方法，並以 `@Transactional` 包覆。
7. 執行架構檢查、後端測試及受影響 API 驗證；發現違規就修正後重跑。

## 強制邊界

- `controller`：只做 HTTP mapping、Request validation、呼叫 Service、轉成 `ResponseBodyDto<Response DTO>`。
- `service`：只放業務能力 interface；不得放 Spring concrete class。
- `service/impl`：放業務規則、授權、交易、冪等、跨表協調及 domain exception。
- `persistence`：Java package 放 MyBatis Mapper interface 與 row model；SQL 只放 resources 下同 feature 的 Mapper XML，不得回傳 Controller 專用 response wrapper。
- `dto`：分開 Request、Response 與必要的 application command/query；不得用資料庫 Entity 當 API contract。
- `domain`：放狀態、值物件及不依賴 Spring/MyBatis 的領域規則。
- 固定封閉的 `code + 繁中說明`：只在所屬 `domain` enum 定義，並提供 `code()`、`description()`、嚴格 `fromCode()`；未知代碼不得靜默轉成 `null` 或自造說明。
- 固定錯誤定義：以領域 `ErrorCode` enum 保存 code 與 message，例外只接受 enum。
- Function 註解：public／protected Java method 使用 Javadoc；private、Vue 與 TypeScript function 說明其規則、狀態變化或副作用，不寫只重述名稱的註解。
- `common`：只放真正跨功能的技術契約，例如統一回應與全域例外；郵遞區號、客戶等業務能力不得放入 `common`。

依賴方向固定為：

```text
Controller -> Service interface <- Service implementation -> Persistence Mapper -> MySQL
       |                                  |
       +---------- Request/Response DTO   +---------- Domain/Persistence model
```

禁止 Controller 注入 Mapper、Mapper 依賴 Service、任何 Java class 含 `SELECT`／`INSERT`／`UPDATE`／`DELETE` SQL 或 JDBC、Java Mapper 使用 SQL annotation、功能 Java class 直接散落在 `<feature>/` 根目錄，以及引入 JPA/Hibernate 取代 MyBatis。SQL 動詞只允許出現在 Mapper XML。

動態商品、郵遞區號或營運可維護代碼仍使用資料庫 code table。固定 enum 與動態 code table 必須先分類，不得同一代碼同時維護兩份繁中名稱。

## 驗證

先執行確定性架構檢查：

```bash
python3 skills/enforce-mybatis-three-layer/scripts/check_layers.py create-api/src/main/java/tw/com/insurance/api
```

再執行 repository 的 Maven Wrapper 測試與必要整合測試。檢查不是完整 Java parser；仍須人工確認 DTO／Entity 邊界、transaction 是否涵蓋完整業務動作，以及 Mapper SQL 是否和 schema 逐欄一致。

## 完成條件

- 每個功能都符合 package-by-feature 目錄。
- 依賴只向內流動，Controller 不碰 persistence。
- 寫入交易集中於 Service implementation。
- SQL 只存在 Mapper，且無未綁定使用者輸入。
- compile、test、API contract 與真實 MySQL 行為依風險完成驗證。
