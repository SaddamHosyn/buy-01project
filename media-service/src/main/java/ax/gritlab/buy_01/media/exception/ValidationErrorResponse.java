package ax.gritlab.buy_01.media.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a validation error response with field-specific errors.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    /**
     * The timestamp when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * The HTTP status code of the error.
     */
    private int status;

    /**
     * The error type or category.
     */
    private String error;

    /**
     * The overall error message.
     */
    private String message;

    /**
     * Map of field names to their specific error messages.
     */
    private Map<String, String> fieldErrors;
}
