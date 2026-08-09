package tw.com.insurance.api.customer.service;

import static tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerResult;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerPage;

public interface CustomerService {
	CustomerResult create(CreateCustomerRequest request, String requestId, String reviewerId);
	CustomerPage findPage(int page, int pageSize, String sort);
}
