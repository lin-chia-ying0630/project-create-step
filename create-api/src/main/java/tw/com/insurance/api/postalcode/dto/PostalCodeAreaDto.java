package tw.com.insurance.api.postalcode.dto;

public record PostalCodeAreaDto(String postalCode, String zipCode3, String city, String district, String addressPrefix,
		String halfWidthAddressPrefix) {
}
