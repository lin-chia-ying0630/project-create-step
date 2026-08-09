package tw.com.insurance.api.review.domain;

import tw.com.insurance.api.common.BusinessException;

/** 集中管理覆核狀態與 Maker-Checker 職務分離規則。 */
public final class ReviewDecisionPolicy {
	private ReviewDecisionPolicy() {
	}

	/** 驗證案件仍可決行，且覆核人不是原送審人。 */
	public static void validate(String status, String makerId, String reviewerId) {
		if (!"PENDING".equals(status)) {
			throw new BusinessException(ReviewErrorCode.ALREADY_DECIDED);
		}
		if (reviewerId.equals(makerId)) {
			throw new BusinessException(ReviewErrorCode.MAKER_CANNOT_REVIEW);
		}
	}
}
