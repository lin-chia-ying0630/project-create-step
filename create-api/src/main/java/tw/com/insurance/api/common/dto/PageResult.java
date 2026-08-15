package tw.com.insurance.api.common.dto;

import java.util.List;

/** 統一列表 API 的後端分頁回傳契約。 */
public record PageResult<T>(List<T> items, long totalItems, int page, int pageSize, int totalPages) {
}
