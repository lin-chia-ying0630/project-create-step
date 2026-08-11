package tw.com.insurance.api.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tw.com.insurance.api.common.error.CommonErrorCode;

@RestControllerAdvice
public class ApiExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	ResponseEntity<ResponseBodyDto<Void>> handleBusiness(BusinessException exception) {
		HttpStatus status = exception.errorCode().contains("-404")
				? HttpStatus.NOT_FOUND
				: exception.errorCode().contains("-409") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status)
				.body(ResponseBodyDto.failure(exception.errorCode(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ResponseBodyDto<Void>> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("輸入資料格式錯誤");
		return ResponseEntity.badRequest()
				.body(ResponseBodyDto.failure(CommonErrorCode.INVALID_REQUEST.code(), message));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ResponseBodyDto<Void>> handleUnexpected(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseBodyDto
				.failure(CommonErrorCode.SYSTEM_FAILURE.code(), CommonErrorCode.SYSTEM_FAILURE.message()));
	}
}
