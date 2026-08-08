package tw.com.insurance.api.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class CustomerDtos {
    private CustomerDtos() {}
    public record CreateCustomerRequest(@NotBlank String identityTypeCode,
        @NotBlank @Size(max=20) String identityNo,@NotBlank @Size(max=100) String customerName,
        @NotBlank @Size(max=16) String genderCode,@NotNull LocalDate birthDate,
        @NotBlank @Size(min=2,max=2) String nationalityCode,
        @NotBlank @Size(min=2,max=2) String residencyCountryCode,
        @NotBlank @Size(max=30) @Pattern(regexp="[0-9+() -]{8,30}") String mobilePhone,
        @NotBlank @Size(max=254) @Email String email,
        @NotBlank @Size(max=10) String postalCode,@NotBlank @Size(max=300) String contactAddress,
        @NotBlank @Size(max=32) String occupationCode,@NotBlank @Size(max=32) String sourceOfFundsCode,
        @NotBlank @Size(max=32) String insurancePurposeCode,@NotBlank @Size(max=32) String consentVersion) {}
    public record CustomerResult(String customerId,String identityTypeCode,String maskedIdentityNo,
        String customerName,String genderCode,LocalDate birthDate,String maskedMobilePhone,String maskedEmail,
        String recordStatus,long recordVersion) {}
}
