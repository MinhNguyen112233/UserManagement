package com.example.practice_spring_boot.exception;

import com.example.practice_spring_boot.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException e) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException e) {

        ErrorCode errorCode = e.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setMessage(errorCode.getMessage());
        apiResponse.setCode(errorCode.getCode());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    //xử lý lỗi khi không được phép truy cập
    //MethodArgumentNotValidException trong Spring là một loại ngoại lệ
    // thường xảy ra khi một yêu cầu HTTP đến một endpoint (điểm cuối)
    // không hợp lệ do các ràng buộc kiểm tra dữ liệu (data validation constraints)
    // không được thỏa mãn.
    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // kiểm tra lỗi không hợp lệ dữ liệu
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        Map<String, Object> attributes = null;

        try{
            //Giúp xác định loại lỗi dựa trên giá trị đã được định nghĩa trước trong enum ErrorCode.
            // Nếu enumKey không phải là một giá trị hợp lệ của ErrorCode, đoạn mã này sẽ ném ra một
            // IllegalArgumentException, và mã lỗi sẽ giữ giá trị mặc định (ErrorCode.INVALID_KEY).
            errorCode = ErrorCode.valueOf(enumKey);

            //Phần này lấy thông tin về ràng buộc kiểm tra từ lỗi đầu tiên trong danh sách lỗi của yêu cầu.
            //exception.getBindingResult().getAllErrors().getFirst(): Lấy tất cả các lỗi được phát hiện trong
            // yêu cầu và chọn lỗi đầu tiên.
            //unwrap(ConstraintViolation.class): Chuyển đổi lỗi này thành một đối tượng ConstraintViolation,
            // cho phép truy cập vào các thuộc tính chi tiết của ràng buộc kiểm tra (như các ràng buộc
            // @NotNull, @Size, v.v.).
            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            // Các thuộc tính này có thể chứa thông tin cụ thể về các điều kiện của ràng buộc,
            // chẳng hạn như giá trị tối thiểu (min), giá trị tối đa (max), và các thông tin khác
            // cần thiết để cung cấp phản hồi lỗi chính xác hơn cho người dùng.
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

        }catch(IllegalArgumentException e){

        }

        //=> lấy được ra giá trị min trong  @Size(min = 4, message = "USERNAME_INVALID")
        //để đẩy vào trong ErrorCode
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                Objects.nonNull(attributes)
                ?mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage()
        );

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
