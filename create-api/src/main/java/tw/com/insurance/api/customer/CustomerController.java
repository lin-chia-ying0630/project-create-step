package tw.com.insurance.api.customer;

import static tw.com.insurance.api.customer.CustomerDtos.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import tw.com.insurance.api.common.ResponseBodyDto;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service){this.service=service;}
    @PostMapping ResponseBodyDto<CustomerResult> create(@Valid @RequestBody CreateCustomerRequest request,
        @RequestHeader(value="Idempotency-Key",required=false) String requestId){
        String effectiveRequestId=requestId==null||requestId.isBlank()?java.util.UUID.randomUUID().toString():requestId;
        return ResponseBodyDto.success("客戶資料建立成功",service.create(request,effectiveRequestId));
    }
}
