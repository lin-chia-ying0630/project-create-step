package tw.com.insurance.api.postalcode.service.impl;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import tw.com.insurance.api.postalcode.dto.PostalCodeAreaDto;
import tw.com.insurance.api.postalcode.persistence.PostalCodeMapper;
import tw.com.insurance.api.postalcode.service.PostalCodeService;

@Service
public class PostalCodeServiceImpl implements PostalCodeService {
	private final PostalCodeMapper mapper;

	public PostalCodeServiceImpl(PostalCodeMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public PostalCodeAreaDto findArea(String postalCode) {
		String normalized = postalCode.trim();
		String zipCode3 = normalized.substring(0, 3);
		Map<String, Object> code = mapper.findZipCode3(zipCode3);
		if (code == null) {
			throw new NoSuchElementException("找不到郵遞區號前三碼: " + zipCode3);
		}
		String[] area = String.valueOf(code.get("code_after")).split("\\|", -1);
		if (area.length != 2 || area[0].isBlank() || area[1].isBlank()) {
			throw new IllegalStateException("郵遞區號代碼定義格式錯誤: " + zipCode3);
		}
		return new PostalCodeAreaDto(normalized, zipCode3, area[0], area[1], area[0] + area[1],
				(String) code.get("code_description"));
	}
}
