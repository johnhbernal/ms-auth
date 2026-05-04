package co.com.practica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static co.com.practica.auth.constants.AppConstants.*;

/**
 * Standard API response wrapper.
 * Every endpoint returns this object for consistency.
 *
 * <p>Usage example:
 * <pre>{@code
 *   return ResponseEntity.ok(ApiResponse.ok(data));
 *   return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private String code;
    private String description;
    private Object data;

    /** 200 OK — successful request with response body. */
    public static ApiResponse ok(Object data) {
        return ApiResponse.builder()
                .code(CODE_OK)
                .description(HttpStatus.OK.name())
                .data(data)
                .build();
    }

    /** 201 Created — resource created successfully. */
    public static ApiResponse created(Object data) {
        return ApiResponse.builder()
                .code(CODE_CREATED)
                .description(HttpStatus.CREATED.name())
                .data(data)
                .build();
    }

    /** 400 Bad Request — invalid input or validation failure. */
    public static ApiResponse badRequest(String description) {
        return ApiResponse.builder()
                .code(CODE_BAD_REQUEST)
                .description(description != null ? description : HttpStatus.BAD_REQUEST.name())
                .data(null)
                .build();
    }

    /** 404 Not Found — requested resource does not exist. */
    public static ApiResponse notFound(String description) {
        return ApiResponse.builder()
                .code(CODE_NOT_FOUND)
                .description(description != null ? description : HttpStatus.NOT_FOUND.name())
                .data(null)
                .build();
    }

    /** 500 Internal Server Error — unexpected server-side failure. */
    public static ApiResponse internalError(String description) {
        return ApiResponse.builder()
                .code(CODE_ERROR)
                .description(description != null ? description : HttpStatus.INTERNAL_SERVER_ERROR.name())
                .data(null)
                .build();
    }

    /** Generic error — use when the HTTP status is determined at runtime. */
    public static ApiResponse error(String code, String description) {
        return ApiResponse.builder()
                .code(code)
                .description(description)
                .data(null)
                .build();
    }
}
