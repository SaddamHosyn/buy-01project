package ax.gritlab.buy_01.user.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Error response structure for validation errors.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    /** Timestamp when the validation error occurred. */
    private LocalDateTime timestamp;
    /** HTTP status code. */
    private int status;
    /** Error type or category. */
    private String error;
    /** General error message. */
    private String message;
    /** Map of field names to validation error messages. */
    private Map<String, String> fieldErrors;
}

