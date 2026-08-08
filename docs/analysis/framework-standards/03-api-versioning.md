# API 版本控管規範

> 適用範圍：所有對外 REST API 的版本策略、路徑規則、棄用流程與 OpenAPI 維護方式。

## 1. 版本策略

採用 **URL 路徑版本**（Path Versioning），格式為 `/api/v{N}/`：

```
/api/v1/policies/{policyNo}
/api/v2/policies/{policyNo}   ← 新版本，舊版本進入棄用期
```

- 版本號為正整數，從 `v1` 起。
- 不使用 Header 版本（`Accept: application/vnd.pos.v2+json`），降低前端整合複雜度。
- 內部服務對服務呼叫（如未來微服務化）可使用 Header 版本，另行定義。

## 2. 何時需要新版本

| 情境 | 是否需要新版本 |
|---|---|
| 新增非必填欄位（向後相容） | 否，直接加在現有版本 |
| 修改現有欄位名稱（canonical name 遷移） | **是**，舊版本維持舊名，新版本使用新名 |
| 移除欄位或改變欄位語意 | **是** |
| 新增必填欄位 | **是** |
| 改變 HTTP 狀態碼語意 | **是** |
| 新增 API 端點 | 否，加在現有版本 |

## 3. 相容期規則

- 新版本正式上線後，舊版本進入 **棄用期，最短 3 個月**。
- 棄用期間舊版本仍正常服務，但回應 Header 加上棄用警告：
  ```
  Deprecation: true
  Sunset: 2026-11-08
  Link: </api/v2/policies>; rel="successor-version"
  ```
- 棄用期結束前通知所有 API 使用者（含前端團隊與外部整合方）。
- 棄用期結束後回傳 `410 Gone`，body 說明遷移目標版本。

## 4. 遷移期雙版本實作策略

```java
// 舊版本 Controller（v1）保留，內部轉換後委派給新版本 Service
@RestController
@RequestMapping("/api/v1/policies")
@Deprecated
public class PolicyV1Controller {

    @GetMapping("/{policyNo}")
    public ResponseBodyDto<PolicyV1Response> getPolicy(@PathVariable String policyNo) {
        // 呼叫共用 Service，將回傳結果轉換為舊欄位名稱（例如 rideList → 舊名稱）
        return ResponseBodyDto.success(v1Adapter.toV1Response(policyService.getPolicy(policyNo)));
    }
}
```

- **不得**為舊版本複製一份獨立 Service 邏輯。
- Service 層只有一份，由 Adapter 負責欄位名稱轉換。
- 舊版本 Controller 類別加 `@Deprecated` 標記。

## 5. OpenAPI 規格維護

### 規格位置

```
pos-api/
  src/main/resources/openapi/
    v1/
      insurance-application.yaml
      policy-inquiry.yaml
      policy-change.yaml
      underwriting.yaml
      claims.yaml
      premium.yaml
    v2/
      ...（新版本建立後放此）
```

### 維護規則

- OpenAPI 規格為**唯一 API 契約來源**，後端實作不得偏離規格。
- 每次 API 變更必須先更新 OpenAPI 規格，再改程式。
- 採 design-first：版本控制中的靜態 OpenAPI YAML 是唯一權威來源，並優先由其生成 Java／TypeScript transport 型別。
- `springdoc-openapi` 的 runtime 輸出只作瀏覽與 drift 驗證，不得反過來覆蓋權威 YAML；CI 必須比較兩者的 path、schema 與 required 欄位。
- PR 時必須包含 OpenAPI diff，Reviewer 確認契約變更範圍。

### 標記棄用端點

```yaml
paths:
  /api/v1/policies/{policyNo}:
    get:
      deprecated: true
      description: "已棄用，請改用 /api/v2/policies/{policyNo}，將於 2026-11-08 停止服務。"
```

## 6. 前端版本切換規則

- 前端 API 呼叫統一透過 `src/api/` 目錄的 typed client 函式，不得在 component 直接寫路徑字串。
- 版本升級時只需修改 `src/api/` 對應函式的路徑，不改 component。
- API base URL 透過環境變數注入（`.env.development`、`.env.production`），不寫死。
