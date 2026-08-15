package tw.com.insurance.api.review.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/** 覆核 API 的 request／response transport contract。 */
public final class ReviewDtos {
	private ReviewDtos() {
	}
	public record ReviewSubmissionResult(String reviewId, String operationType, String operationDescription,
			String businessKey, String reviewStatus, LocalDateTime submittedAt) {
	}
	public record ReviewSummary(String reviewId, String operationType, String operationDescription, String businessKey,
			String reviewStatus, String makerId, LocalDateTime submittedAt, String reviewerId,
			LocalDateTime reviewedAt) {
	}
	public record ReviewDetail(String reviewId, String operationType, String operationDescription, String businessKey,
			String reviewStatus, String makerId, LocalDateTime submittedAt, String reviewerId, String reviewComment,
			LocalDateTime reviewedAt, JsonNode payload, JsonNode result) {
	}
	public record ReviewDecisionRequest(@Size(max = 500) String comment) {
	}
	public record ReviewPageResult(List<ReviewSummary> items, long totalItems, int page, int pageSize, int totalPages) {
	}
	public record ReviewOperationOption(String value, String label) {
	}
}
