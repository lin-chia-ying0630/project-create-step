package tw.com.insurance.api.customer.service;

import static tw.com.insurance.api.customer.dto.CustomerDtos.CreateCustomerRequest;
import static tw.com.insurance.api.customer.dto.CustomerDtos.CustomerResult;

public interface CustomerService {
	CustomerResult create(CreateCustomerRequest request, String requestId);
}
