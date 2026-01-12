package ax.gritlab.buy_01.product.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Validation error response DTO.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public final class ValidationErrorResponse {
    /**
     * Timestamp of the error.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Error type.
     */
    private String error;

    /**
     * Error message.
     */
    private String message;

    /**
     * Field-specific validation errors.
     */
    private Map<String, String> fieldErrors;
}
