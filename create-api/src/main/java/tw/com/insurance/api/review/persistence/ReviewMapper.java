package tw.com.insurance.api.review.persistence;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 覆核案件與待審鎖的 MyBatis persistence contract。 */
@Mapper
public interface ReviewMapper {
	int insertPendingLock(@Param("functionCode") String functionCode, @Param("businessKey") String businessKey,
			@Param("reviewId") String reviewId);
	int insertCase(@Param("reviewId") String reviewId, @Param("operationType") String operationType,
			@Param("functionCode") String functionCode, @Param("businessKey") String businessKey,
			@Param("payload") byte[] payload, @Param("makerId") String makerId);
	List<Map<String, Object>> findPage(@Param("status") String status, @Param("offset") int offset,
			@Param("pageSize") int pageSize);
	long countByStatus(@Param("status") String status);
	Map<String, Object> findById(@Param("reviewId") String reviewId);
	Map<String, Object> findByIdForUpdate(@Param("reviewId") String reviewId);
	int approve(@Param("reviewId") String reviewId, @Param("reviewerId") String reviewerId,
			@Param("comment") String comment, @Param("resultJson") String resultJson, @Param("version") long version);
	int reject(@Param("reviewId") String reviewId, @Param("reviewerId") String reviewerId,
			@Param("comment") String comment, @Param("version") long version);
	int deletePendingLock(@Param("reviewId") String reviewId);
	int insertAudit(@Param("auditId") String auditId, @Param("reviewId") String reviewId,
			@Param("operationType") String operationType, @Param("operatorId") String operatorId,
			@Param("requestId") String requestId, @Param("resultCode") String resultCode);
}
