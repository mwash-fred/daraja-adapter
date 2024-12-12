package apps.wmn.daraja.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API Response wrapper")
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Object error
) {
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        return new ApiResponse<>(false, null, message, errorDetails);
    }
}
