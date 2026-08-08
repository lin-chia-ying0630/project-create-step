package tw.com.insurance.api.postalcode.persistence;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostalCodeMapper {
	Map<String, Object> findZipCode3(@Param("zipCode3") String zipCode3);
}
