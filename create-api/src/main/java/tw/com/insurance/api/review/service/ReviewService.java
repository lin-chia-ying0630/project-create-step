package tw.com.insurance.api.review.service;

import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDecisionRequest;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewDetail;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewPageResult;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewOperationOption;
import static tw.com.insurance.api.review.dto.ReviewDtos.ReviewSubmissionResult;
import tw.com.insurance.api.review.domain.ReviewOperationType;
import java.util.List;

/** 提供跨功能 Maker-Checker 送審、查詢與決行能力。 */
public interface ReviewService {
	ReviewSubmissionResult submit(ReviewOperationType operationType, String businessKey, Object payload,
			String makerId);
	ReviewPageResult findPage(String status, int page, int pageSize, String sort, String query, String operationType);
	List<ReviewOperationOption> findOperationOptions();
	ReviewDetail findById(String reviewId);
	ReviewDetail approve(String reviewId, ReviewDecisionRequest request, String reviewerId, String requestId);
	ReviewDetail reject(String reviewId, ReviewDecisionRequest request, String reviewerId, String requestId);
}
