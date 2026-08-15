# MyBatis package-by-feature 參考

## 標準目錄

```text
<java-package-root>/
├── common/
│   ├── ApiExceptionHandler.java
│   ├── BusinessException.java
│   └── ResponseBodyDto.java
└── <feature>/
    ├── controller/
    │   └── <Feature>Controller.java
    ├── dto/
    │   ├── <Feature>CreateRequest.java
    │   └── <Feature>Response.java
    ├── domain/
    │   └── <Feature>Status.java
    ├── persistence/
    │   ├── <Feature>Mapper.java
    │   └── <Feature>Row.java
    └── service/
        ├── <Feature>Service.java
        └── impl/
            └── <Feature>ServiceImpl.java
```

沒有實際內容的選用目錄不必建立。MyBatis XML 放在 `src/main/resources/mapper/<feature>/`，`namespace` 必須與 Mapper interface 完整類名一致，statement `id` 必須與 interface method 名一致。Java Mapper 禁止 SQL annotation。

## 類別與資料契約

| 類型 | 可依賴 | 不可負責 |
|---|---|---|
| Controller | Service interface、Request/Response DTO、共用回應 | SQL、交易、業務規則 |
| Service interface | application/domain DTO | Spring Controller、Mapper implementation |
| Service implementation | Service interface、Domain、Mapper | HTTP response 格式、SQL 字串 |
| Mapper | persistence model、明確 query parameter | 業務流程、HTTP、`ResponseBodyDto` |
| Domain | Java 標準型別及同領域物件 | Spring、MyBatis、Vue |

固定代碼 enum 範例：

```java
public enum FeatureStatus {
    PENDING("P", "待處理"),
    COMPLETED("C", "已完成");

    private final String code;
    private final String description;
}
```

實際 enum 必須補齊 constructor、`code()`、`description()` 與嚴格 `fromCode()`。若代碼會由營運人員新增或改名，則不屬於封閉 enum，應存入資料庫 code table。

## 新功能順序

1. Flyway migration 與 constraint。
2. persistence model 與 Mapper contract。
3. Request／Response DTO。
4. Service interface 與 implementation。
5. Controller 與 OpenAPI。
6. 前端 type、typed API client 與 view；只有跨 route 狀態確有需要時才建立 Store。
7. Mapper real-MySQL test、Service test、API contract test 與 UI 驗證。

## 重構舊目錄

1. 盤點現有 class、Spring bean、MyBatis namespace、測試及未提交變更。
2. 一個功能一次搬移，使用 `Move to` 保留歷史可讀性。
3. 修正 package、import、constructor type 及 MyBatis XML namespace。
4. Controller 改依賴 Service interface；concrete class 移至 `service/impl`。
5. 每完成一個功能立即 compile/test，避免累積不可定位的錯誤。
