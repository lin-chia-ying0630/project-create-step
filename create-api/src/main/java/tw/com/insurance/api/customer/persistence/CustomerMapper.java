package tw.com.insurance.api.customer.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {
	int insertCustomer(@Param("id") String id, @Param("customerType") String customerType, @Param("name") String name,
			@Param("gender") String gender, @Param("birthDate") LocalDate birthDate,
			@Param("nationality") String nationality, @Param("residency") String residency);

	int insertOrganizationProfile(@Param("customerId") String customerId,
			@Param("establishmentDate") LocalDate establishmentDate,
			@Param("responsiblePersonName") String responsiblePersonName, @Param("industryCode") String industryCode,
			@Param("organizationTypeCode") String organizationTypeCode);

	int insertIdentity(@Param("id") String id, @Param("customerId") String customerId, @Param("type") String type,
			@Param("hash") String hash, @Param("ciphertext") byte[] ciphertext, @Param("last4") String last4,
			@Param("country") String country);

	int insertContact(@Param("id") String id, @Param("customerId") String customerId, @Param("type") String type,
			@Param("ciphertext") byte[] ciphertext, @Param("hash") String hash, @Param("masked") String masked);

	int insertAddress(@Param("id") String id, @Param("customerId") String customerId,
			@Param("postalCode") String postalCode, @Param("ciphertext") byte[] ciphertext,
			@Param("masked") String masked);

	int insertNameHistory(@Param("id") String id, @Param("customerId") String customerId, @Param("name") String name);

	int insertKyc(@Param("id") String id, @Param("customerId") String customerId,
			@Param("occupation") String occupation, @Param("funds") String funds, @Param("purpose") String purpose);

	int insertConsent(@Param("id") String id, @Param("customerId") String customerId, @Param("version") String version);

	int insertAudit(@Param("id") String id, @Param("customerId") String customerId,
			@Param("requestId") String requestId);
	int markReviewed(@Param("customerId") String customerId, @Param("reviewerId") String reviewerId);
	long countCustomers();
	List<Map<String, Object>> findCustomerPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
			@Param("sortField") String sortField, @Param("sortDirection") String sortDirection);
}
