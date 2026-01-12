package ax.gritlab.buy_01.user.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure for API errors.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;
    /** HTTP status code. */
    private int status;
    /** Error type or category. */
    private String error;
    /** Detailed error message. */
    private String message;
}

