package tw.com.insurance.api.postalcode.service;

import tw.com.insurance.api.postalcode.dto.PostalCodeAreaDto;

public interface PostalCodeService {
	PostalCodeAreaDto findArea(String postalCode);
}
