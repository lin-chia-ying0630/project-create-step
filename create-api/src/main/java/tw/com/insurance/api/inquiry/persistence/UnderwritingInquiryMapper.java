package tw.com.insurance.api.inquiry.persistence;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UnderwritingInquiryMapper {
	Map<String, Object> findInquiry(String query);

	List<Map<String, Object>> findItems(@Param("inquiryNo") String inquiryNo);
	long countInquiries();
	List<Map<String, Object>> findInquiryPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
			@Param("sortField") String sortField, @Param("sortDirection") String sortDirection);
}
