package tw.com.insurance.api.common.util;

import java.util.Set;

/** 經過頁碼邊界與排序白名單驗證的共用查詢參數。 */
public record PageSortRequest(int page, int pageSize, int offset, String sortField, String sortDirection) {

	/**
	 * 將外部查詢參數轉為安全的分頁與排序值。
	 *
	 * @param page 使用者要求頁碼，從 1 起算
	 * @param pageSize 使用者要求每頁筆數，上限 100
	 * @param sort 格式為 field,direction 的排序字串
	 * @param allowedSortFields 該功能允許的排序欄位
	 * @param defaultSortField 未提供或不合法時使用的預設欄位
	 * @return 可直接交給 persistence 層的安全參數
	 */
	public static PageSortRequest of(int page, int pageSize, String sort, Set<String> allowedSortFields,
			String defaultSortField) {
		int safePage = Math.max(page, 1);
		int safePageSize = Math.min(Math.max(pageSize, 1), 100);
		String[] sortParts = sort == null ? new String[0] : sort.split(",", 2);
		String requestedField = sortParts.length == 0 ? "" : sortParts[0];
		String safeField = allowedSortFields.contains(requestedField) ? requestedField : defaultSortField;
		String safeDirection = sortParts.length == 2 && "desc".equalsIgnoreCase(sortParts[1]) ? "desc" : "asc";
		return new PageSortRequest(safePage, safePageSize, (safePage - 1) * safePageSize, safeField,
				safeDirection);
	}

	/** 依總筆數計算總頁數，空清單回傳 0。 */
	public int totalPages(long totalItems) {
		return (int) Math.ceil((double) totalItems / pageSize);
	}
}
