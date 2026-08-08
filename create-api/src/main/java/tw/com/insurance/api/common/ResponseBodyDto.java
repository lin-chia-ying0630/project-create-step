package tw.com.insurance.api.common;

public record ResponseBodyDto<T>(boolean success, String message, String errorCode, String errorMessage, T data) {
    public static <T> ResponseBodyDto<T> success(String message, T data) {
        return new ResponseBodyDto<>(true, message, null, null, data);
    }

    public static <T> ResponseBodyDto<T> failure(String errorCode, String errorMessage) {
        return new ResponseBodyDto<>(false, null, errorCode, errorMessage, null);
    }
}
